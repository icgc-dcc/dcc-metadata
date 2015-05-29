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

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Properties;

import lombok.Cleanup;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

import org.icgc.dcc.metadata.client.model.Entity;

@RequiredArgsConstructor
public class ManifestWriter {

  @NonNull
  private final File inputDir;
  @NonNull
  private final File outputDir;

  @SneakyThrows
  public void writeManifest(String manifestFileName, @NonNull List<Entity> entities) {
    val manifest = new Properties();

    for (val entity : entities) {
      val filePath = getFilePath(entity);

      manifest.put(entity.getId(), filePath);
    }

    val comments = "ICGC file manifest for " + inputDir.getName();

    @Cleanup
    val output = new FileOutputStream(new File(outputDir, manifestFileName));
    manifest.store(output, comments);
  }

  @SneakyThrows
  private String getFilePath(Entity entity) {
    val file = new File(inputDir, entity.getFileName());
    return file.getCanonicalPath();
  }

}
