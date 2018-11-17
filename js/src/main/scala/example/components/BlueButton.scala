package example.components

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

object BlueButton {

  case class Props(text: String, onClick: Callback)

  val component = ScalaComponent.builder[Props]("BlueButton")
    .render_P(props => <.button(^.cls := "pure-button", ^.onClick --> props.onClick, props.text))
    .build

  def apply(props: Props) = component(props)
}
