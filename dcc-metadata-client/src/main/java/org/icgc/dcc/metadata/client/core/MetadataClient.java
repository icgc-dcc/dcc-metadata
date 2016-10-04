/*
 * Copyright (c) 2015 The Ontario Institute for Cancer Research. All rights reserved.                             
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

import static java.lang.System.out;
import static org.icgc.dcc.common.core.util.Formats.formatCount;

import java.io.File;
import java.util.List;

import org.icgc.dcc.metadata.client.manifest.ManifestService;
import org.icgc.dcc.metadata.client.manifest.ManifestWriter;
import org.icgc.dcc.metadata.client.manifest.RegisterManifest.ManifestEntry;
import org.icgc.dcc.metadata.client.service.EntityRegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@NoArgsConstructor
@AllArgsConstructor
public class MetadataClient {

  @Autowired
  private EntityRegistrationService registrationService;

  @Autowired
  private ManifestService manifestService;

  @SneakyThrows
  public void register(File manifest, File outputDir) {
    out.printf("Reading %s \n", manifest.getCanonicalPath());

    val manifestFiles = manifestService.getUploadManifest(manifest);

    log.info("Read {} files", formatCount(manifestFiles.getEntries()));

    // Register entries in Manifest and update with object id
    register(manifestFiles.getEntries());

    // need to add object id's to RegisterManifest
    val manifestWriter = new ManifestWriter(outputDir);

    manifestWriter.writeManifest(manifestFiles.getEntries());
  }

  protected void register(List<ManifestEntry> manifestFiles) {
    int counter = 1;

    for (val file : manifestFiles) {
      val entity = registrationService.register(file);
      file.setObjectId(entity.getId());
      out.printf("[%d/%d] Registered %s%n", counter++, manifestFiles.size(), file.getFileName());
    }
  }

}
