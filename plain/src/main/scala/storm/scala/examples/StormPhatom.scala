package storm.scala.examples

import org.apache.storm.topology.{BasicOutputCollector, OutputFieldsDeclarer, TopologyBuilder, BoltDeclarer, IRichSpout, IBasicBolt}
// import org.apache.storm.topology.base.{BaseRichSpout, BaseBasicBolt}
import org.apache.storm.task.TopologyContext

object StormPhantom {
  // abstract case class TStormSpout[OutTup](spout:StormSpout)
  // will run into overide keyword problem. let's use trait

  trait StormSpoutT[Out]{def spout:IRichSpout}

  // abstract case class TStormBolt[InTup,OutTup](bolt:StormBolt)

  trait StormBoltT[In,Out]{def bolt:IBasicBolt}


  abstract class TopEmpty
  abstract class TopWithSpout extends TopEmpty
  abstract class TopWithBolt extends TopWithSpout

  case class TopologyBuilderT[+State,Out](builder:TopologyBuilder,output_name:String) {
    def createTopology() = builder.createTopology()

    def init : TopologyBuilderT[TopEmpty,Out] =
      new TopologyBuilderT(builder,output_name)

    def >> [NextOut]
      ( spout_name:String
      , ts:StormSpoutT[NextOut]
      , threadMax:Int)
      (implicit evS:State <:< TopEmpty) : TopologyBuilderT[TopWithSpout,NextOut] = {

        builder.setSpout(spout_name, ts.spout, threadMax)
        new TopologyBuilderT[TopWithSpout,NextOut](builder,spout_name)
      }


    def >>> [NextOut,State <: TopWithSpout]
      ( bolt_name:String
      , tb:StormBoltT[Out,NextOut]
      , threadMax:Int)
      ( inDecl: BoltDeclarer => BoltDeclarer )
      (implicit evS: State <:< TopWithSpout) : TopologyBuilderT[TopWithBolt,NextOut] = {
        val i = builder.setBolt(bolt_name,tb.bolt, threadMax)
        inDecl(i)
        new TopologyBuilderT[TopWithBolt,NextOut](builder,bolt_name)
      } // TODO: test other inDec with different field selection
  }

}
