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
package org.icgc.dcc.metadata.server.service;

import org.icgc.dcc.common.core.util.UUID5;
import org.icgc.dcc.metadata.server.model.Entity;
import org.icgc.dcc.metadata.server.repository.EntityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Joiner;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class EntityService {

  /**
   * Dependencies.
   */
  @Autowired
  private final EntityRepository repository;

  @NonNull
  public Entity register(Entity entity) {
    log.info("Registering {}...", entity);
    val existing = repository.findByGnosIdAndFileName(entity.getGnosId(), entity.getFileName());
    if (existing != null) {
      throw new DuplicateEntityException(existing);
    }

    val id = resolveFileId(entity.getGnosId(), entity.getFileName());
    entity.setId(id);
    entity.setCreatedTime(System.currentTimeMillis());

    val registered = repository.save(entity);
    log.info("Successfully registered {}", entity);

    return registered;
  }

  /**
   * Generates UUID that is consistent with:
   * 
   * <pre>
   * https://github.com/icgc-dcc/dcc-etl/blob/7630b0e7d5e70781ca6a26176bc84781ffc3cd60/dcc-etl-repo/src/main/java/org/icgc/dcc/etl/repo/core/RepositoryFileProcessor.java#L42
   * </pre>
   */
  private static String resolveFileId(String... parts) {
    return UUID5.fromUTF8(UUID5.getNamespace(), Joiner.on('/').join(parts)).toString();
  }

}
