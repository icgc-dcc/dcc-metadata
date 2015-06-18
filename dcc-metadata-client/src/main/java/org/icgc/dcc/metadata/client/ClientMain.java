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
package org.icgc.dcc.metadata.client;

import static com.google.common.base.Objects.firstNonNull;
import static com.google.common.base.Strings.repeat;
import static java.lang.System.err;
import static java.lang.System.out;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.icgc.dcc.metadata.client.cli.ClientOptions;
import org.icgc.dcc.metadata.client.core.MetadataClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

@Slf4j
@SpringBootApplication
public class ClientMain {

  /**
   * Constants.
   */
  public static final String APPLICATION_NAME = "dcc-metadata-client";
  public static final int SUCCESS_STATUS_CODE = 0;
  public static final int FAILURE_STATUS_CODE = 1;

  public static void main(String... args) {
    val options = new ClientOptions();
    val cli = new JCommander(options);
    cli.setAcceptUnknownOptions(true);
    cli.setProgramName(APPLICATION_NAME);

    try {
      cli.parse(args);
      if (options.help) {
        usage(cli);
        return;
      }

      if (options.version) {
        version();
        return;
      }

      if (options.inputDir == null) {
        err.println("The input directory is unset.");
        return;
      }

      if (options.outputDir == null) {
        err.println("The output directory is unset.");
        return;
      }

      banner("Running with {}", options);
      execute(options, args);
    } catch (ParameterException e) {
      log.error("Invalid parameter(s): ", e);
      err.println("Invalid parameter(s): " + e.getMessage());
      usage(cli);
    } catch (Exception e) {
      log.error("Unknown error: ", e);
      err.println("Unknow error. Please check the log for detailed error messages: " + e.getMessage());
      System.exit(FAILURE_STATUS_CODE);
    }
  }

  private static void execute(ClientOptions options, String[] args) {
    val context = SpringApplication.run(ClientMain.class, args);
    val client = context.getBean(MetadataClient.class);
    log.info("{}\n", repeat("-", 100));

    client.register(options.inputDir, options.outputDir, options.manifestFileName);
  }

  private static void usage(JCommander cli) {
    val message = new StringBuilder();
    cli.usage(message);
    err.println(message.toString());
  }

  private static void banner(String message, Object... args) {
    log.info("{}", repeat("-", 100));
    log.info(message, args);
    log.info("{}", repeat("-", 100));
  }

  private static void version() {
    val version = firstNonNull(ClientMain.class.getPackage().getImplementationVersion(), "[unknown version]");
    out.printf("DCC Metadata Client%nVersion %s%n", version);
  }

}
