package controllers

import javax.inject.Inject

import dao.UserDao
import models.User
import play.api.data.Form
import play.api.data.Forms.{longNumber, mapping, nonEmptyText, optional}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc.{Action, Controller}
import views.html

import scala.concurrent.Future

/**
  * Created by sromic on 19/01/16.
  */

class UserController @Inject() (val userDao: UserDao, val messagesApi: MessagesApi) extends Controller with I18nSupport {

  /** This result directly redirect to the application home.*/
  val Home = Redirect(routes.UserController.list(0, 2, ""))

  /** Describe the user form (used in both edit and create screens).*/
  val userForm = Form(
    mapping(
      "id" -> optional(longNumber),
      "firstName" -> nonEmptyText,
      "lastName" -> nonEmptyText)(User.apply)(User.unapply)
  )


  /** Display the paginated list of users.
    *
    * @param page Current page number (starts from 0)
    * @param orderBy Column to be sorted
    * @param filter Filter applied on user names
    */
  def list(page: Int, orderBy: Int, filter: String) = Action.async { implicit request =>
    val users = userDao.list(page = page, orderBy = orderBy, filter = ("%" + filter + "%"))
    users.map(cs => Ok(html.list(cs, orderBy, filter)))
  }

  /** Display the 'new user form'. */
  def create = Action{ implicit rs =>
      Ok(html.createForm(userForm))
  }

  /** Handle the 'new user form' submission. */
  def save = Action.async { implicit rs =>
    userForm.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest(html.createForm(formWithErrors))),
      user => {
        for {
          _ <- userDao.insert(user)
        } yield Home.flashing("success" -> "User %s has been created".format(user.fistName))
      })
  }

}
