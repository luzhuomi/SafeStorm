package storm.scala.examples

import storm.scala.dsl._
import backtype.storm.Config
import backtype.storm.LocalCluster
import backtype.storm.topology.TopologyBuilder
// import backtype.storm.topology.InputDeclarer
import backtype.storm.topology.BoltDeclarer
import backtype.storm.tuple.{Fields, Tuple, Values}
import collection.mutable.{Map, HashMap}
import util.Random






object WordCountTopologyP {

  // abstract case class TStormSpout[OutTup](spout:StormSpout) 
  // will run into overide keyword problem. let's use trait

  trait StormSpoutT[Out]{def spout:StormSpout}


  // abstract case class TStormBolt[InTup,OutTup](bolt:StormBolt)

  trait StormBoltT[In,Out]{def bolt:StormBolt}


  abstract class TopEmpty;
  abstract class TopWithSpout extends TopEmpty;
  abstract class TopWithBolt extends TopWithSpout;


  case class TopologyBuilderT[+State,+Out](builder:TopologyBuilder,output_name:String) {
    def createTopology = builder.createTopology
  }






  case class RandomSentenceSpoutT (spout: RandomSentenceSpout) extends StormSpoutT[String]
  class RandomSentenceSpout extends StormSpout(outputFields = List("sentence")) {
    val sentences = List("the cow jumped over the moon",
                         "an apple a day keeps the doctor away",
                         "four score and seven years ago",
                         "snow white and the seven dwarfs",
                         "i am at two with nature")
    def nextTuple = {
      Thread sleep 100
      emit (sentences(Random.nextInt(sentences.length)))
    }
  }





  // An example of using matchSeq for Scala pattern matching of Storm tuples
  // plus using the emit and ack DSLs.
  case class SplitSentenceT (bolt:SplitSentence) extends StormBoltT[String,String]
  class SplitSentence extends StormBolt(outputFields = List("word")) {
    def execute(t: Tuple) = t matchSeq {
      case Seq(sentence: String) => sentence split " " foreach
        { word => using anchor t emit (word) }
      t ack
    }
  }


  case class WordCountT (bolt:WordCount) extends StormBoltT[String,(String,Int)]
  class WordCount extends StormBolt(List("word", "count")) {
    var counts: Map[String, Int] = _
    setup {
      counts = new HashMap[String, Int]().withDefaultValue(0)
    }
    def execute(t: Tuple) = t matchSeq {
      case Seq(word: String) =>
        counts(word) += 1
        using anchor t emit (word, counts(word))
        t ack
    }
  }


  def addSpout[Out](top:TopologyBuilderT[TopEmpty,_])
    ( spout_name:String
    , ts:StormSpoutT[Out]
    , threadMax:Int) : TopologyBuilderT[TopWithSpout,Out] = 
    top match {
      case TopologyBuilderT(builder,_) => {
        builder.setSpout(spout_name, ts.spout, threadMax)
        TopologyBuilderT[TopWithSpout,Out](builder,spout_name)   
      }
    }

  
  def addBolt[In,Out,State <: TopWithSpout](top:TopologyBuilderT[State,In])
    ( bolt_name:String
    , tb:StormBoltT[In,Out]
    , threadMax:Int) 
    ( inDecl: BoltDeclarer => BoltDeclarer ) : TopologyBuilderT[TopWithBolt,Out] = 
    top match {
      case TopologyBuilderT(builder,output_name) => {
        val i = builder.setBolt(bolt_name,tb.bolt, threadMax)
        inDecl(i)
        TopologyBuilderT[TopWithBolt,Out](builder,bolt_name)   
      }
    } // TODO: test other inDec with different field selection
    


  def main(args: Array[String]) = {
    /*

    val builder = new TopologyBuilder

    builder.setSpout("randsentence", new RandomSentenceSpout, 5)
    builder.setBolt("split", new SplitSentence, 8)
        .shuffleGrouping("randsentence")
    builder.setBolt("count", new WordCount, 12)
        .fieldsGrouping("split", new Fields("word"))
    */

    val builderT = addBolt(
                    addBolt(addSpout(new TopologyBuilderT(new TopologyBuilder,""))("randsentence", new RandomSentenceSpoutT(new RandomSentenceSpout), 8)) 
                      ("split", new SplitSentenceT(new SplitSentence), 8)( _.shuffleGrouping("randsentence"))
                  ) ("count", new WordCountT(new WordCount), 12)( _.fieldsGrouping("split", new Fields("word")))


    val conf = new Config
    conf.setDebug(true)
    conf.setMaxTaskParallelism(3)

    val cluster = new LocalCluster
    cluster.submitTopology("word-count", conf, builderT.createTopology)
    Thread sleep 10000
    cluster.shutdown
  }
}