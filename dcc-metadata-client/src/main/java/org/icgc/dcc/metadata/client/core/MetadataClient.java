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

import static org.icgc.dcc.common.core.util.FormatUtils.formatCount;

import java.io.File;
import java.io.IOException;
import java.util.List;

import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.icgc.dcc.metadata.client.core.GNOSFileDirectoryReader.GNOSFile;
import org.icgc.dcc.metadata.client.model.Entity;
import org.icgc.dcc.metadata.client.service.EntityRegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableList;

@Slf4j
@Component
public class MetadataClient {

  @Autowired
  private EntityRegistrationService registrationServive;

  @SneakyThrows
  public void register(File inputDir, File outputDir, String manifestFileName) {
    log.info("Regisitering: inputDir: {}, outputDir: {}, manifestFileName: {}",
        inputDir.getCanonicalPath(),
        outputDir.getCanonicalPath(),
        manifestFileName);

    log.info("Reading '{}' for files...", inputDir.getCanonicalPath());
    val files = readFiles(inputDir);
    log.info("Read {} files", formatCount(files));

    val entities = register(files);

    val manifestWriter = new ManifestWriter(inputDir, outputDir);
    manifestWriter.writeManifest(manifestFileName, entities);
  }

  private List<Entity> register(List<GNOSFile> files) {
    val registeredEntities = ImmutableList.<Entity> builder();

    for (val file : files) {
      val entity = registrationServive.register(file.getGnosId(), file.getFileName());
      registeredEntities.add(entity);
    }

    return registeredEntities.build();
  }

  private List<GNOSFile> readFiles(File gnosDir) throws IOException {
    return new GNOSFileDirectoryReader().readFiles(gnosDir);
  }

}
