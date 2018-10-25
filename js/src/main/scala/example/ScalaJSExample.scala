package example

import example.shared.HelloShared
import japgolly.scalajs.react._
import org.scalajs.dom.html

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}
import japgolly.scalajs.react.vdom.html_<^._

@JSExportTopLevel("ScalaJSExample")
object ScalaJSExample {

  @JSExport
  def main(target: html.Div): Unit = {
    val Button = ScalaComponent.builder[ReactEventFromInput => Callback]("Button")
      .render_P(click => <.button(^.cls := "pure-button", ^.onClick ==> click, "click me!"))
      .build

    val Header = ScalaComponent.builder[Unit]("Header")
      .renderStatic(
        <.div(^.cls := "header",
          <.div(^.cls := "home-menu pure-menu pure-menu-horizontal pure-menu-fixed",
            <.a(^.cls := "pure-menu-heading", ^.href := "", "Your site"),
            <.ul(^.cls := "pure-menu-list",
              <.li(^.cls := "pure-menu-item pure-menu-selected",
                <.a(^.cls := "pure-menu-link", ^.href := "#", "Home"),
              ),
              <.li(^.cls := "pure-menu-item",
                <.a(^.cls := "pure-menu-link", ^.href := "#", "Sign Up"),
              ),
            )
          )
        )
      )
      .build

    case class State(clicks: Long)
    class MainContentBackend($: BackendScope[Unit, State]) {
      def tick(e: ReactEventFromInput) = $.modState(s => State(s.clicks + 1))
      def render(s: State) =
        <.div(^.cls := "content-wrapper",
          <.div(^.cls := "content-head is-center",
            "Hello: " + HelloShared.TEST_STR
          ),
          <.div(^.cls := "content",
            <.div(^.cls := "l-box pure-g is-center",
              <.div(^.cls := "l-box pure-u-1 pure-u-md-1-2", Button(tick)),
              <.div(^.cls := "l-box pure-u-1 pure-u-md-1-2", " clicks: " + s.clicks)
            )
          ),
          <.div(^.cls := "footer l-box is-center",
            "footer"
          )
        )
    }

    val MainContent = ScalaComponent.builder[Unit]("MainContent")
      .initialState(State(0))
      .renderBackend[MainContentBackend]
      .build

    val Hello = ScalaComponent.builder[Unit]("Hello")
      .renderStatic(
        React.Fragment(
          Header(),
          MainContent()
        )
      )
      .build

    Hello().renderIntoDOM(target)
  }
}
