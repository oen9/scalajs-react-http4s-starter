package example.services

import diode.{Action, ActionHandler, Circuit, ModelRW}
import diode.react.ReactConnector

case class Clicks(count: Int)
case class RootModel(clicks: Clicks)

case object IncreaseClicks extends Action

class ClicksHandler[M](modelRW: ModelRW[M, Clicks]) extends ActionHandler(modelRW) {
  override def handle = {
    case IncreaseClicks => updated(value.copy(count = value.count + 1))
  }
}

object AppCircuit extends Circuit[RootModel] with ReactConnector[RootModel] {
  override protected def initialModel: RootModel = RootModel(Clicks(0))

  override protected def actionHandler: AppCircuit.HandlerFunction = composeHandlers(
    new ClicksHandler(zoomTo(_.clicks))
  )
}
