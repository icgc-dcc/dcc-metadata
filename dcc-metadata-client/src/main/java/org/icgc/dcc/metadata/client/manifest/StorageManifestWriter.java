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
package org.icgc.dcc.metadata.client.manifest;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.icgc.dcc.metadata.client.manifest.Manifest.ManifestEntry;

import com.google.common.io.Files;

import lombok.Cleanup;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

@RequiredArgsConstructor
public class StorageManifestWriter {

  private final static String HEADER = "object-id\tfile-path\tmd5-checksum\n"; // Don't forget the line break!

  @NonNull
  private final File outputDir;

  @SneakyThrows
  public void writeManifest(@NonNull List<ManifestEntry> list) {
    if (list.isEmpty()) {
      return;
    }

    // Name manifest with GNOS id
    val gnosId = list.get(0).getGnosId();

    File storageManifest = new File(outputDir, gnosId);

    @Cleanup
    val writer = Files.newWriter(storageManifest, StandardCharsets.UTF_8);
    writer.write(HEADER);

    for (val entity : list) {
      writer.write(getStorageManifestLine(entity));
    }
  }

  @SneakyThrows
  public String getStorageManifestLine(ManifestEntry entry) {
    return entry.getObjectId()
        .concat("\t")
        .concat(entry.getFileName())
        .concat("\t")
        .concat(entry.getFileMd5sum())
        .concat("\n");
  }

}
