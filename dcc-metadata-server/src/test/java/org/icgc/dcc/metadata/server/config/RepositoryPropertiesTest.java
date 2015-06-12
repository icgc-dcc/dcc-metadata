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
package org.icgc.dcc.metadata.server.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.icgc.dcc.metadata.server.config.RepositoryProperties.DEFAULT_DATABASE;
import static org.junit.rules.ExpectedException.none;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.google.common.collect.ImmutableList;

public class RepositoryPropertiesTest {

  RepositoryProperties props;

  @Rule
  public ExpectedException thrown = none();

  @Before
  public void setUp() {
    props = new RepositoryProperties();
    props.setServer("localhost");
  }

  @Test
  public void noServerTest() {
    props.setServer(null);
    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("Mongo server url is not set or empty");
    props.createUrl();
  }

  @Test
  public void credentialsTest() {
    props.setUser("u");
    props.setPassword("p");
    assertThat(props.createUrl()).isEqualTo("mongodb://u:p@localhost/" + DEFAULT_DATABASE);
  }

  @Test
  public void credentialsTest_noUser() {
    props.setPassword("p");
    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("Can't resolve Mongo credentials. Username or password are null or empty");
    props.createUrl();
  }

  @Test
  public void credentialsTest_noPassword() {
    props.setUser("u");
    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("Can't resolve Mongo credentials. Username or password are null or empty");
    props.createUrl();
  }

  @Test
  public void databaseTest() {
    props.setDatabase("db");
    assertThat(props.createUrl()).isEqualTo("mongodb://localhost/db");
  }

  @Test
  public void databaseTest_empty() {
    props.setDatabase("");
    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("Mongo database is not defined");
    props.createUrl();
  }

  @Test
  public void replicaTest_one() {
    props.setReplica(ImmutableList.of("localhost2"));
    assertThat(props.createUrl()).isEqualTo("mongodb://localhost,localhost2/" + DEFAULT_DATABASE);
  }

  @Test
  public void replicaTest_two() {
    props.setReplica(ImmutableList.of("localhost2:123", "localhost3"));
    assertThat(props.createUrl()).isEqualTo("mongodb://localhost,localhost2:123,localhost3/" + DEFAULT_DATABASE);
  }

}
