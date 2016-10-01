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

import static org.icgc.dcc.metadata.core.http.Headers.ENTITY_ID_HEADER;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.ResponseEntity.ok;

import java.util.Map;

import javax.validation.Valid;

import org.icgc.dcc.metadata.server.model.Entity;
import org.icgc.dcc.metadata.server.repository.EntityRepository;
import org.icgc.dcc.metadata.server.service.DuplicateEntityException;
import org.icgc.dcc.metadata.server.service.EntityService;
import org.icgc.dcc.metadata.server.util.HeadMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.val;

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

  @HeadMapping("/{id}")
  public ResponseEntity<Entity> exists(@PathVariable("id") String id) {
    return repository.exists(id) ? ok(null) : notFound();
  }

  @GetMapping("/{id}")
  public ResponseEntity<Entity> get(@PathVariable("id") String id) {
    val entity = repository.findOne(id);
    return entity != null ? ok(entity) : notFound();
  }

  @GetMapping
  public ResponseEntity<Page<Entity>> find(
      @RequestParam Map<String, String> params,
      @PageableDefault(sort = "id") Pageable pageable) {
    return ok(repository.findAll(params, pageable));
  }

  @PostMapping
  public ResponseEntity<Entity> register(@RequestBody @Valid Entity entity) {
    try {
      return ok(service.register(entity));
    } catch (DuplicateEntityException e) {
      return conflict(e);
    }
  }

  private static ResponseEntity<Entity> notFound() {
    return new ResponseEntity<>(NOT_FOUND);
  }

  private static ResponseEntity<Entity> conflict(DuplicateEntityException e) {
    val headers = new HttpHeaders();
    headers.set(ENTITY_ID_HEADER, e.getExisting().getId());

    return new ResponseEntity<>(headers, CONFLICT);
  }

}
