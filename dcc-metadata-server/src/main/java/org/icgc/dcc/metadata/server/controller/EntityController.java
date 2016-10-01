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
package org.icgc.dcc.metadata.server.controller;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.icgc.dcc.metadata.core.http.Headers.ENTITY_ID_HEADER;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.web.bind.annotation.RequestMethod.HEAD;

import javax.validation.Valid;

import org.icgc.dcc.metadata.server.model.Entity;
import org.icgc.dcc.metadata.server.repository.EntityRepository;
import org.icgc.dcc.metadata.server.service.DuplicateEntityException;
import org.icgc.dcc.metadata.server.service.EntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/entities")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class EntityController {

  /**
   * Dependencies.
   */
  @Autowired
  private final EntityRepository repository;
  @Autowired
  private final EntityService service;

  @GetMapping("/{id}")
  public ResponseEntity<Entity> get(@PathVariable("id") String id) {
    val entity = repository.findOne(id);
    if (entity == null) {
      return new ResponseEntity<>(NOT_FOUND);
    }

    return ResponseEntity.ok(entity);
  }

  @GetMapping
  public ResponseEntity<Page<Entity>> find(
      @RequestParam(required = false) String gnosId,
      @RequestParam(required = false) String fileName,
      @RequestParam(required = false) String projectCode,
      @PageableDefault(sort = { "id" }) Pageable pageable) {
    log.debug("/find service invoked: gnosId = {}, fileName = {}, projectCode = {}", gnosId, fileName, projectCode);

    Page<Entity> entities = null;

    // Note - If both gnosId and projectCode are encountered, only gnosId is used
    if (isNullOrEmpty(gnosId) && isNullOrEmpty(fileName) && isNullOrEmpty(projectCode)) {
      entities = repository.findAll(pageable);
    } else if (isNullOrEmpty(gnosId)) { // gnosId is null
      if (isNullOrEmpty(projectCode)) { // gnosId and projectCode is null
        entities = repository.findByFileName(fileName, pageable);
      } else if (isNullOrEmpty(fileName)) { // projectCode not null, filename is null
        entities = repository.findByProjectCode(projectCode, pageable);
      } else { // projectCode is not null, fileName is not null
        entities = repository.findByProjectCodeAndFileName(projectCode, fileName, pageable);
      }
    } else { // gnosId not null - supercedes project code
      if (isNullOrEmpty(fileName)) {
        entities = repository.findByGnosId(gnosId, pageable);
      } else {
        entities = repository.findByGnosIdAndFileName(gnosId, fileName, pageable);
      }
    }

    return ResponseEntity.ok(entities);
  }

  @RequestMapping(value = "/{id}", method = HEAD)
  public ResponseEntity<?> exists(@PathVariable("id") String id) {
    return repository.exists(id) ? ResponseEntity.ok(null) : new ResponseEntity<>(NOT_FOUND);
  }

  @PostMapping
  public ResponseEntity<Entity> register(@RequestBody @Valid Entity entity) {
    try {
      return ResponseEntity.ok(service.register(entity));
    } catch (DuplicateEntityException e) {
      return new ResponseEntity<>(createConflictHeaders(e.getExisting()), CONFLICT);
    }
  }

  private static MultiValueMap<String, String> createConflictHeaders(Entity entity) {
    val headers = new HttpHeaders();
    headers.set(ENTITY_ID_HEADER, entity.getId());

    return headers;
  }
}
