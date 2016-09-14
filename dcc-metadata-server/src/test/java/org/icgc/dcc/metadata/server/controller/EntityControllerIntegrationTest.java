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
package org.icgc.dcc.metadata.server.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import org.icgc.dcc.metadata.server.repository.EntityRepository;
import org.icgc.dcc.metadata.server.service.EntityService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(MockitoJUnitRunner.class)
public class EntityControllerIntegrationTest {

  private static final String GNOS_ID_1 = "g123";
  private static final String FILE_NAME_1 = "f123";

  @Mock
  private EntityRepository repository;

  @Mock
  EntityService service;

  @InjectMocks
  EntityController controller;

  private MockMvc mockMvc;

  @Before
  public void setUp() {
    mockMvc = standaloneSetup(controller)
        .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
        .build();
  }

  /**
   * Trigger validation by registering a new Entity via REST client to endpoint
   * @throws Exception
   */
  @Test
  public void testValidation_missingProjectCode() throws Exception {
    // trigger validation - only happens when POSTing to endpoint
    // doesn't work if invoking EntityController directly
    mockMvc.perform(post("/entities")
        .contentType(APPLICATION_JSON)
        .content(createEntityAsString(GNOS_ID_1, FILE_NAME_1)))
        .andExpect(status().is4xxClientError());
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
}
