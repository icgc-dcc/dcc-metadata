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
package org.icgc.dcc.metadata.server.query;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.CriteriaDefinition;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.query.MongoEntityInformation;
import org.springframework.data.mongodb.repository.support.SimpleMongoRepository;
import org.springframework.util.Assert;

import lombok.val;

public class SimpleQueryMongoRepository<T, ID extends Serializable> extends SimpleMongoRepository<T, ID>
    implements QueryExecutor<T, ID> {

  private final MongoOperations mongoOperations;
  private final MongoEntityInformation<T, ID> entityInformation;

  public SimpleQueryMongoRepository(MongoEntityInformation<T, ID> metadata, MongoOperations mongoOperations) {
    super(metadata, mongoOperations);
    this.entityInformation = metadata;
    this.mongoOperations = mongoOperations;
  }

  @Override
  public <S extends T> Page<S> findAll(Query query, Pageable pageable) {
    val q = query.with(pageable);

    val count = mongoOperations.count(q, getCollectionName());
    if (count == 0) {
      return new PageImpl<S>(Collections.<S> emptyList());
    }

    return new PageImpl<S>(mongoOperations.find(q, getType(), getCollectionName()), pageable, count);
  }

  @Override
  public <S extends T> Page<S> findAll(CriteriaDefinition criteria, Pageable pageable) {
    return findAll(getQuery(criteria), pageable);
  }

  @Override
  public <S extends T> List<S> findAll(CriteriaDefinition criteria, Sort sort) {
    return mongoOperations.find(getQuery(criteria).with(sort), getType(), getCollectionName());
  }

  @Override
  public <S extends T> List<S> findAll(CriteriaDefinition criteria) {
    return findAll(criteria, (Sort) null);
  }

  @Override
  public <S extends T> S findOne(CriteriaDefinition criteria) {
    return mongoOperations.findOne(getQuery(criteria), getType(), getCollectionName());
  }

  @Override
  public <S extends T> long count(CriteriaDefinition criteria) {
    return mongoOperations.count(getQuery(criteria), getType(), getCollectionName());
  }

  @Override
  public <S extends T> boolean exists(CriteriaDefinition criteria) {
    return mongoOperations.exists(getQuery(criteria), getType(), getCollectionName());
  }

  private Query getQuery(CriteriaDefinition criteria) {
    Assert.notNull(criteria, "Criteria must not be null!");
    return new Query(criteria);
  }

  private String getCollectionName() {
    return entityInformation.getCollectionName();
  }

  @SuppressWarnings("unchecked")
  private <S extends T> Class<S> getType() {
    return (Class<S>) entityInformation.getJavaType();
  }

}
