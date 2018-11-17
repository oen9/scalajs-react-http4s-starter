package example.modules

import example.components.BlueButton
import example.shared.HelloShared
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

object Home {

  case class State(clicks: Long)

  class Backend($: BackendScope[Unit, State]) {
    def tick(): Callback = $.modState(s => State(s.clicks + 1))

    def render(s: State) =
      React.Fragment(
        <.div(^.cls := "content-head is-center",
          "Hello: " + HelloShared.TEST_STR
        ),
        <.div(^.cls := "content",
          <.div(^.cls := "l-box pure-g is-center",
            <.div(^.cls := "l-box pure-u-1 pure-u-md-1-2", BlueButton(BlueButton.Props("click me!", tick()))),
            <.div(^.cls := "l-box pure-u-1 pure-u-md-1-2", " clicks: " + s.clicks)
          )
        )
      )
  }

  val component = ScalaComponent.builder[Unit]("Home")
    .initialState(State(0))
    .renderBackend[Backend]
    .build

  def apply() = component()
}
