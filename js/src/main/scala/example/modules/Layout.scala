package example.modules

import example.ScalaJSExample.{AboutLoc, HomeLoc, Loc}
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router.{Resolution, RouterCtl}
import japgolly.scalajs.react.vdom.html_<^._

object Layout {

  case class MenuItem(idx: Int, label: String, location: Loc)

  case class Props(router: RouterCtl[Loc], resolution: Resolution[Loc])

  val menuItems = Seq(
    MenuItem(0, "Home", HomeLoc),
    MenuItem(1, "About", AboutLoc)
  )

  val component = ScalaComponent.builder[Props]("Layout")
    .render_P(props => {
      React.Fragment(
        <.div(^.cls := "header",
          <.div(^.cls := "home-menu pure-menu pure-menu-horizontal pure-menu-fixed",
            props.router.link(HomeLoc)(^.cls := "pure-menu-heading", "Your site"),
            <.ul(^.cls := "pure-menu-list",
              menuItems.toVdomArray(i => {
                <.li(^.key := i.idx, ^.cls := "pure-menu-item", (^.cls := "pure-menu-selected").when(i.location == props.resolution.page),
                  props.router.link(i.location)(^.cls := "pure-menu-link", i.label)
                )
              })
            )
          )
        ),
        <.div(^.cls := "content-wrapper",
          props.resolution.render(),
          <.div(^.cls := "footer l-box is-center",
            "footer"
          )
        )
      )
    })
    .build

  def apply(ctl: RouterCtl[Loc], resolution: Resolution[Loc]) = component(Props(ctl, resolution))
}
