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

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.lang.String.format;
import static org.hamcrest.Matchers.is;
import static org.icgc.dcc.metadata.core.http.Headers.ENTITY_ID_HEADER;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.head;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import org.icgc.dcc.metadata.server.model.Entity;
import org.icgc.dcc.metadata.server.repository.EntityRepository;
import org.icgc.dcc.metadata.server.service.DuplicateEntityException;
import org.icgc.dcc.metadata.server.service.EntityService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;

import com.google.common.collect.ImmutableList;

import lombok.val;

@RunWith(MockitoJUnitRunner.class)
public class EntityControllerTest {

  private static final String ID_1 = "123";
  private static final String ID_2 = "321";
  private static final String GNOS_ID_1 = "g123";
  private static final String GNOS_ID_2 = "g321";
  private static final String FILE_NAME_2 = "f321";
  private static final String FILE_NAME_1 = "f123";
  private static final String PROJECT_CODE_1 = "PROJ-CD1";
  private static final String PROJECT_CODE_2 = "CODE-PRJ";
  private static final long CREATED_TIME = 0;

  @Mock
  EntityRepository repository;
  @Mock
  EntityService service;

  Entity responseEntity1;
  Entity responseEntity2;

  @InjectMocks
  EntityController controller;

  private MockMvc mockMvc;

  @Before
  public void setUp() {
    responseEntity1 = createEntity(ID_1, GNOS_ID_1, FILE_NAME_1, PROJECT_CODE_1, CREATED_TIME);
    responseEntity2 = createEntity(ID_2, GNOS_ID_2, FILE_NAME_2, PROJECT_CODE_1, CREATED_TIME);

    mockMvc = standaloneSetup(controller)
        .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
        .build();
  }

  @Test
  public void findTest_all() throws Exception {
    val page = new PageImpl<Entity>(ImmutableList.of(responseEntity1, responseEntity2));
    when(repository.findAll(any(Pageable.class))).thenReturn(page);

    mockMvc.perform(get("/entities"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.numberOfElements", is(2)))
        .andExpect(jsonPath("$.content[0].id", is(ID_1)))
        .andExpect(jsonPath("$.content[1].id", is(ID_2)));
  }

  @Test
  public void findTest_gnosId() throws Exception {
    val page = new PageImpl<Entity>(ImmutableList.of(responseEntity1));
    when(repository.findByGnosId(eq(GNOS_ID_1), any(Pageable.class))).thenReturn(page);

    mockMvc.perform(get("/entities?gnosId=" + GNOS_ID_1))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.numberOfElements", is(1)))
        .andExpect(jsonPath("$.content[0].id", is(ID_1)));
  }

  @Test
  public void findTest_fileName() throws Exception {
    val page = new PageImpl<Entity>(ImmutableList.of(responseEntity1));
    when(repository.findByFileName(eq(FILE_NAME_1), any(Pageable.class))).thenReturn(page);

    mockMvc.perform(get("/entities?fileName=" + FILE_NAME_1))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.numberOfElements", is(1)))
        .andExpect(jsonPath("$.content[0].id", is(ID_1)));
  }

  @Test
  public void findTest_fileName_gnosId() throws Exception {
    val page = new PageImpl<Entity>(ImmutableList.of(responseEntity1));
    when(repository.findByGnosIdAndFileName(eq(GNOS_ID_1), eq(FILE_NAME_1), any(Pageable.class))).thenReturn(page);

    mockMvc.perform(get(format("/entities?fileName=%s&gnosId=%s", FILE_NAME_1, GNOS_ID_1)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.numberOfElements", is(1)))
        .andExpect(jsonPath("$.content[0].id", is(ID_1)));
  }

  @Test
  public void getTest() throws Exception {
    when(repository.findOne(ID_1)).thenReturn(responseEntity1);

    mockMvc.perform(get("/entities/" + ID_1))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(ID_1)));
  }

  @Test
  public void getTest_notFound() throws Exception {
    mockMvc.perform(get("/entities/" + ID_1))
        .andExpect(status().isNotFound());
  }

  @Test
  public void existsTest() throws Exception {
    when(repository.exists(ID_1)).thenReturn(TRUE);
    mockMvc.perform(head("/entities/" + ID_1))
        .andExpect(status().isOk());
  }

  @Test
  public void existsTest_notFound() throws Exception {
    when(repository.exists(ID_1)).thenReturn(FALSE);
    mockMvc.perform(head("/entities/" + ID_1))
        .andExpect(status().isNotFound());
  }

  @Test
  public void registerTest() throws Exception {
    when(service.register(any(Entity.class))).thenReturn(responseEntity1);

    mockMvc.perform(post("/entities")
        .contentType(APPLICATION_JSON)
        .content(createEntityAsString(GNOS_ID_1, FILE_NAME_1, PROJECT_CODE_1)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(ID_1)))
        .andExpect(jsonPath("$.gnosId", is(GNOS_ID_1)))
        .andExpect(jsonPath("$.fileName", is(FILE_NAME_1)))
        .andExpect(jsonPath("$.projectCode", is(PROJECT_CODE_1)))
        .andExpect(jsonPath("$.createdTime", is((int) CREATED_TIME)));
  }

  @Test
  public void registerTest_missingProjectCode() throws Exception {
    // when(service.register(any(Entity.class))).thenReturn(responseEntity1);
    service.register(createEntity(ID_1, GNOS_ID_1, FILE_NAME_1, null, CREATED_TIME));
  }

  @Test
  public void saveTest_duplicate() throws Exception {
    when(repository.findByGnosIdAndFileName(GNOS_ID_1, FILE_NAME_1)).thenReturn(responseEntity1);
    when(service.register(any(Entity.class))).thenThrow(new DuplicateEntityException(responseEntity1));

    mockMvc.perform(post("/entities")
        .contentType(APPLICATION_JSON)
        .content(createEntityAsString(GNOS_ID_1, FILE_NAME_1, PROJECT_CODE_1)))
        .andExpect(status().isConflict())
        .andExpect(header().string(ENTITY_ID_HEADER, ID_1));
  }

  /**
   * Trigger validation by registering a new Entity via REST client to endpoint
   * @throws Exception
   */
  @Test
  public void validationTest_withProjectCode() throws Exception {
    // trigger validation - only happens when POSTing to endpoint
    // doesn't work if invoking EntityController directly
    mockMvc.perform(post("/entities")
        .contentType(APPLICATION_JSON)
        .content(createEntityAsString(GNOS_ID_1, FILE_NAME_1)))
        .andExpect(status().is4xxClientError());
  }

  @Test
  public void validationTest_includingProjectCode() throws Exception {
    // trigger validation - only happens when POSTing to endpoint
    // doesn't work if invoking EntityController directly
    mockMvc.perform(post("/entities")
        .contentType(APPLICATION_JSON)
        .content(createEntityAsString(GNOS_ID_1, FILE_NAME_1, "RANDOM-PROJECT-CD")))
        .andExpect(status().is2xxSuccessful());
  }

  private static String createEntityAsString(String gnosId, String fileName, String projectCode) {
    return format("{\"gnosId\":\"%s\",\"fileName\":\"%s\",\"projectCode\":\"%s\"}", gnosId, fileName, projectCode);
  }

  /**
   * Create Entity as string with no project code
   * @param gnosId
   * @param fileName
   * @return
   */
  private static String createEntityAsString(String gnosId, String fileName) {
    return String.format("{\"gnosId\":\"%s\",\"fileName\":\"%s\"}", gnosId, fileName);
  }

  private static Entity createEntity(String id, String gnosId, String fileName, String projectCode, long createdTime) {
    val result = new Entity();
    result.setId(id);
    result.setGnosId(gnosId);
    result.setFileName(fileName);
    result.setProjectCode(projectCode);
    result.setCreatedTime(createdTime);

    return result;
  }

}
