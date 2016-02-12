package dao

import javax.inject.{Inject, Singleton}

import models.{Page, User}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future

/**
  * Created by sromic on 19/01/16.
  */
@Singleton
class UserDao @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile] {

  import driver.api._

  class Users(tag: Tag) extends Table[User](tag, "USER") {

    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)

    def firstName = column[String]("FIRSTNAME")

    def lastName = column[String]("LASTNAME")

    def * = (id.?, firstName, lastName) <> (User.tupled, User.unapply _)
  }

  private val users = TableQuery[Users]

  /** Count users with a filter. */
  def count(filter: String): Future[Int] = {
    db.run(users.filter { user => user.firstName.toLowerCase like filter.toLowerCase }.length.result)
  }

  /** Insert a new user. */
  def insert(user: User): Future[Unit] =
    db.run(users += user).map(_ => ())

  /** Retrieve a user from the name. */
  def findById(firstName: String): Future[Option[User]] =
    db.run(users.filter(_.firstName === firstName).result.headOption)


  /** Return a page of User */
  def list(page: Int = 0, pageSize: Int = 10, orderBy: Int = 1, filter: String = "%"): Future[Page[User]] = {

    val offset = pageSize * page
    val query =
      (for {
        user <- users
        if user.firstName.toLowerCase like filter.toLowerCase
      } yield (user))
        .drop(offset)
        .take(pageSize)

    for {
      totalRows <- count(filter)
      list = query.result.map { rows => rows.collect { case user: User => user } }
      result <- db.run(list)
    } yield Page(result, page, offset, totalRows)
  }

}
