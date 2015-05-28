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
import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;

import lombok.NonNull;
import lombok.Value;
import lombok.val;

import com.google.common.collect.Lists;

public class GNOSFileDirectoryReader {

  public List<GNOSFile> readFiles(@NonNull File gnosDir) throws IOException {
    val gnosId = getGNOSId(gnosDir);

    val result = Lists.<GNOSFile> newArrayList();
    try (val stream = Files.newDirectoryStream(gnosDir.toPath())) {
      for (val entry : stream) {
        result.add(new GNOSFile(gnosId, entry.toFile().getName()));
      }
    } catch (DirectoryIteratorException e) {
      // I/O error encounted during the iteration, the cause is an IOException
      throw e.getCause();
    }

    return result;
  }

  private static String getGNOSId(File gnosDir) {
    val gnosId = gnosDir.getName();

    try {
      UUID.fromString(gnosId);
      return gnosId;
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid GNOS id directory: '" + gnosId + "'. Not a valid UUID");
    }
  }

  @Value
  public static class GNOSFile {

    String gnosId;
    String fileName;

  }

}
