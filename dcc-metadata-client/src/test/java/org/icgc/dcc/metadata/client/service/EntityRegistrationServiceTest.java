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
package org.icgc.dcc.metadata.client.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import org.icgc.dcc.metadata.client.core.MetadataClientTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.retry.RecoveryCallback;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryState;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestTemplate;

import lombok.val;

@RunWith(MockitoJUnitRunner.class)
public class EntityRegistrationServiceTest {

  @Mock
  private RestTemplate restTemplate;

  @Mock
  private RetryTemplate retryTemplate;

  @InjectMocks
  EntityRegistrationService sut;

  public final static String PATH_NAME = "/path/to/the/actual/file.dat";
  public final static String FILE_NAME = "file.dat";

  @Test(expected = IllegalArgumentException.class)
  public void test_scrub_handle_empty_path_gracefully() {
    val entry = MetadataClientTest.createEntry("gnos-id-1", "PROJ", "", "md5", "controlled");
    val entity = sut.buildEntity(entry);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_scrub_handle_null_path_gracefully() {
    val entry = MetadataClientTest.createEntry("gnos-id-1", "PROJ", null, "md5", "controlled");
    val entity = sut.buildEntity(entry);
  }

  @Test
  public void test_path_removal() {
    val entry = MetadataClientTest.createEntry("gnos-id-1", "PROJ", PATH_NAME, "md5", "controlled");
    val entity = sut.buildEntity(entry);
    assertThat(entity.getFileName()).isEqualTo(FILE_NAME);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void test_register_removes_path() throws Throwable {
    val entry = MetadataClientTest.createEntry("gnos-id-1", "PROJ", PATH_NAME, "md5", "controlled");
    val entity = sut.buildEntity(entry);

    // Stub out call to remote service. We are really verifying behaviour that the path-removal logic is being
    // invoked during register()
    when(retryTemplate.execute(any(RetryCallback.class), any(RecoveryCallback.class), any(RetryState.class)))
        .thenReturn(entity);

    val result = sut.register(entry);
    assertThat(result.getFileName()).isEqualTo(FILE_NAME);
  }

}
