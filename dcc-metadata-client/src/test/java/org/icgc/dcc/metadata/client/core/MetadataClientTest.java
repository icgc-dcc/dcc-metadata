/*
 * Copyright (c) 2016 The Ontario Institute for Cancer Research. All rights reserved.                             
 *                                                                                                               
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * You should have received a copy of the GNU General Public License along with                                  
 * this program. If not, see <http://www.gnu.org/licenses/>.                                                     
 *                                                                                                               
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY                           
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES                          
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT                           
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,                                
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED                          
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;                               
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER                              
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN                         
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.icgc.dcc.metadata.client.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.icgc.dcc.metadata.client.manifest.ManifestService;
import org.icgc.dcc.metadata.client.manifest.RegisterManifest;
import org.icgc.dcc.metadata.client.manifest.RegisterManifest.ManifestEntry;
import org.icgc.dcc.metadata.client.model.Entity;
import org.icgc.dcc.metadata.client.service.EntityRegistrationService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.base.Splitter;

import lombok.val;

@RunWith(MockitoJUnitRunner.class)
public class MetadataClientTest {

  protected final static String GNOS_ID = "gnos1";
  protected final static String PROJECT_CODE = "PROJ";
  protected final static String FILE_PATH = "/path/to/";
  protected final static String ACCESS = "controlled";

  @Rule
  public TemporaryFolder tmp = new TemporaryFolder();

  @Mock
  EntityRegistrationService restClient;

  @Spy
  ManifestService manifestService;

  MetadataClient sut;

  public static ManifestEntry createEntry(String gnosId, String projectCode, String fileName, String md5,
      String access) {
    return ManifestEntry.builder()
        .gnosId(gnosId)
        .projectCode(projectCode)
        .fileName(fileName)
        .fileMd5sum(md5)
        .access(access)
        .build();
  }

  public List<ManifestEntry> createManifestEntryStubs() {
    val result = new ArrayList<ManifestEntry>();

    val me1 = createEntry(GNOS_ID, PROJECT_CODE, FILE_PATH + "file1", "md5_1", ACCESS);
    val me2 = createEntry(GNOS_ID, PROJECT_CODE, FILE_PATH + "file2", "md5_2", ACCESS);
    val me3 = createEntry(GNOS_ID, PROJECT_CODE, FILE_PATH + "file3", "md5_3", ACCESS);

    result.add(me1);
    result.add(me2);
    result.add(me3);

    return result;
  }

  @Before
  public void setup() {
    sut = new MetadataClient(restClient, manifestService);
  }

  @Test
  public void test_registration_returns_object_ids() {

    val entries = createManifestEntryStubs();
    val e1 =
        new Entity().setId("object-id-1").setGnosId(GNOS_ID).setProjectCode(PROJECT_CODE)
            .setFileName(FILE_PATH + "file1").setAccess(ACCESS);
    val e2 =
        new Entity().setId("object-id-2").setGnosId(GNOS_ID).setProjectCode(PROJECT_CODE)
            .setFileName(FILE_PATH + "file2").setAccess(ACCESS);
    val e3 =
        new Entity().setId("object-id-3").setGnosId(GNOS_ID).setProjectCode(PROJECT_CODE)
            .setFileName(FILE_PATH + "file3").setAccess(ACCESS);

    // Stub out calls to actual Metadata service
    when(restClient.register(entries.get(0))).thenReturn(e1);
    when(restClient.register(entries.get(1))).thenReturn(e2);
    when(restClient.register(entries.get(2))).thenReturn(e3);

    sut.register(entries);

    assertThat(entries.get(0).getObjectId()).isEqualTo("object-id-1");
    assertThat(entries.get(1).getObjectId()).isEqualTo("object-id-2");
    assertThat(entries.get(2).getObjectId()).isEqualTo("object-id-3");
  }

  @Test
  public void test_write_to_file() throws IOException {

    // Load test case
    val manifestFile = new File("src/test/resources/register-manifest.txt");
    val manifestEntries = manifestService.getUploadManifest(manifestFile);

    // Grab gnos id from input file (and set up stubs)
    val gnosId = setupMetadataStubs(manifestEntries);

    // Execute method under test using test case
    sut.register(manifestFile, tmp.getRoot());

    // Load generated manifest and see what's what
    val generatedManifestFile = new File(tmp.getRoot(), gnosId);

    List<String> contents = Collections.emptyList();

    try (Stream<String> lines = Files.lines(generatedManifestFile.toPath())) {
      contents = lines.collect(Collectors.toList());
    }

    assertThat(contents.size()).isEqualTo(4);

    // First line should be the header
    val row0 = parseLine(contents.get(0));
    assertThat(row0.size()).isEqualTo(3);
    assertThat(row0.get(0)).isEqualToIgnoringCase("object-id");
    assertThat(row0.get(1)).isEqualToIgnoringCase("file-path");
    assertThat(row0.get(2)).isEqualToIgnoringCase("md5-checksum");

    val row1 = parseLine(contents.get(1));
    assertThat(row1.size()).isEqualTo(3);
    assertThat(row1.get(0)).isEqualToIgnoringCase("object-id-1");
    assertThat(row1.get(1)).isEqualToIgnoringCase(
        "/data/to-be-uploaded/batch107/bb44b6d8-010d-473b-8037-91530a01c24f.seven-samurai.20150918.germline.indel.vcf.gz");
    assertThat(row1.get(2)).isEqualToIgnoringCase("c59cfd7bd38ccdb334c70f675ae18c76");

    val row2 = parseLine(contents.get(2));
    assertThat(row2.size()).isEqualTo(3);
    assertThat(row2.get(0)).isEqualToIgnoringCase("object-id-2");
    assertThat(row2.get(1)).isEqualToIgnoringCase(
        "/data/to-be-uploaded/batch107/bb44b6d8-010d-473b-8037-91530a01c24f.seven-samurai.20150918.germline.indel.vcf.gz.idx");
    assertThat(row2.get(2)).isEqualToIgnoringCase("c09945110eb4e7063d8aff104c4b7aba");

    val row3 = parseLine(contents.get(3));
    assertThat(row3.size()).isEqualTo(3);
    assertThat(row3.get(0)).isEqualToIgnoringCase("object-id-3");
    assertThat(row3.get(1)).isEqualToIgnoringCase(
        "/data/to-be-uploaded/batch107/bb44b6d8-010d-473b-8037-91530a01c24f.metadata.xml");
    assertThat(row3.get(2)).isEqualToIgnoringCase("919e4e8ffb31339718e7be032e8c8a85");
  }

  protected String setupMetadataStubs(RegisterManifest manifest) {
    String gnosId = "";
    int count = 1;
    for (val entry : manifest.getEntries()) {
      if (count == 1) {
        gnosId = entry.getGnosId(); // Need to hang onto gnos id for later (name of output file)
      }
      val objectId = String.format("object-id-%d", count); // Generate object id for test double
      val entity = new Entity().setId(objectId).setGnosId(entry.getGnosId()).setProjectCode(entry.getProjectCode())
          .setFileName(entry.getFileName());
      count++;

      // Stub out calls to actual Metadata service
      when(restClient.register(entry)).thenReturn(entity);
    }
    return gnosId;
  }

  protected static final Splitter LINE_PARSER = Splitter.on('\t').trimResults();

  protected List<String> parseLine(String line) {
    val values = LINE_PARSER.splitToList(line);
    val expectedColumns = 3;
    val actualColumns = values.size();

    assertThat(actualColumns).isEqualTo(expectedColumns);

    return values;
  }
}
