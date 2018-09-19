package org.apache.spark.sql.redis

import org.apache.spark.sql.SaveMode.{Append, ErrorIfExists, Ignore, Overwrite}
import org.apache.spark.sql.sources.{BaseRelation, CreatableRelationProvider, RelationProvider}
import org.apache.spark.sql.{DataFrame, SQLContext, SaveMode}

class DefaultSource extends RelationProvider
  //  with SchemaRelationProvider
  with CreatableRelationProvider {

  override def createRelation(sqlContext: SQLContext,
                              parameters: Map[String, String]): BaseRelation =
    new RedisSourceRelation(sqlContext, parameters, None)

  /**
    * Creates a new relation by saving the data to Redis
    */
  override def createRelation(sqlContext: SQLContext, mode: SaveMode,
                              parameters: Map[String, String], data: DataFrame): BaseRelation = {
    val relation = new RedisSourceRelation(sqlContext, parameters, None)
    mode match {
      case Append => relation.insert(data, overwrite = false)
      case Overwrite => relation.insert(data, overwrite = true)
      case ErrorIfExists =>
        if (relation.nonEmpty) {
          throw new IllegalStateException("SaveMode is set to ErrorIfExists and dataframe " +
            "already exists in Redis and contains data.")
        }
        relation.insert(data, overwrite = false)
      case Ignore =>
        if (relation.isEmpty) {
          relation.insert(data, overwrite = false)
        }
    }

    relation
  }
}
