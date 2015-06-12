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

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Strings.isNullOrEmpty;
import static lombok.AccessLevel.PRIVATE;

import java.util.List;

import lombok.Data;
import lombok.val;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.google.common.base.Joiner;

@Data
@Slf4j
@Configuration
@FieldDefaults(level = PRIVATE)
@ConfigurationProperties(prefix = "mongo", ignoreUnknownFields = false)
public class RepositoryProperties {

  public static final String DEFAULT_DATABASE = "dcc-metadata";

  String user;
  String password;
  String database;
  String server;
  List<String> replica;

  public String createUrl() {
    val resultBuilder = new StringBuilder();
    resultBuilder.append("mongodb://");

    val credentials = resolveCredentials();
    if (credentials != null) {
      resultBuilder.append(credentials + "@");
    }

    checkState(!isNullOrEmpty(server), "Mongo server url is not set or empty");
    resultBuilder.append(server);

    val replicas = resolveReplicas();
    if (replicas != null) {
      resultBuilder.append("," + replicas);
    }

    resultBuilder.append("/" + resolveDatabase());

    val result = resultBuilder.toString();
    log.info("Created MongoURL: {}", result);

    return result;
  }

  private String resolveReplicas() {
    if (replica != null) {
      return Joiner.on(",").join(replica);
    }

    return null;
  }

  private String resolveDatabase() {
    if (database != null) {
      checkState(!isNullOrEmpty(database), "Mongo database is not defined");

      return database;
    }

    return DEFAULT_DATABASE;
  }

  private String resolveCredentials() {
    if (user != null || password != null) {
      checkState(!isNullOrEmpty(user) && !isNullOrEmpty(password),
          "Can't resolve Mongo credentials. Username or password are null or empty");

      return user + ":" + password;
    }

    return null;
  }

}
