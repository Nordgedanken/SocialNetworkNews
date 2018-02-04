package database

import java.sql.Date

import slick.jdbc.SQLiteProfile.api._

// Definition of the TWEETS table
class Tweets(tag: Tag)
    extends Table[(Int, String, String, String, String, Date)](tag, "TWEETS") {
  def id = column[Int]("ID", O.PrimaryKey) // This is the primary key column
  def user = column[String]("USERNAME")
  def text = column[String]("TEXT")
  def url = column[String]("URL")
  def interactions = column[String]("INTERACTIONS")
  def indexingDate = column[Date]("ZIP")

  // Every table needs a * projection with the same type as the table's type parameter
  def * = (id, user, text, url, interactions, indexingDate)
}

class Database {}
