package storm.scala.examples


import org.apache.storm.spout.SpoutOutputCollector
import org.apache.storm.task.TopologyContext
import org.apache.storm.topology.{BasicOutputCollector, OutputFieldsDeclarer, TopologyBuilder}
import org.apache.storm.topology.base.{BaseRichSpout, BaseBasicBolt}
import org.apache.storm.tuple.{Fields, Tuple, Values}
import org.apache.storm.utils.Utils
import org.apache.storm.{Config, LocalCluster, StormSubmitter}

import scala.util.Random

import java.text.BreakIterator

object WordCountTopologyUnTyped  {

  class RandomSentenceSpout extends BaseRichSpout {
    
    var _collector:SpoutOutputCollector = _
    var _rand:Random = _
    
    override def nextTuple(): Unit = {
      Utils.sleep(100)
      val sentences = Array("the cow jumped over the moon","an apple a day keeps the doctor away",
			    "four score and seven years ago","snow white and the seven dwarfs","i am at two with nature")
      val sentence = sentences(_rand.nextInt(sentences.length))
      _collector.emit(new Values(sentence))
    }
    
    override def open(conf: java.util.Map[String, Object], context: TopologyContext, collector: SpoutOutputCollector): Unit = {
      _collector = collector
      _rand = Random
    }

    override def declareOutputFields(declarer: OutputFieldsDeclarer): Unit = {
      declarer.declare(new Fields("word"))
    } 
  }

  class SplitSentenceBolt extends BaseBasicBolt {
    
    override def execute(input: Tuple, collector: BasicOutputCollector): Unit = {
      
      val sentence = input.getString(0)
      val boundary = BreakIterator.getWordInstance
      
      boundary.setText(sentence)
      var start = boundary.first
      var end:Int = start
      
      while(end!=BreakIterator.DONE) {
	
	end = boundary.next
	val word = sentence.substring(start,end).replaceAll("\\s+","")
	start = end
	if(!word.equals("")) {
          collector.emit(new Values(word))
	}
      }
    }
    
    override def declareOutputFields(declarer: OutputFieldsDeclarer): Unit = {
      declarer.declare(new Fields("word"))
    }
  }



  class WordCountBolt extends BaseBasicBolt{
    
    val counts = scala.collection.mutable.Map[String,Int]()
    
    override def execute(input: Tuple, collector: BasicOutputCollector): Unit = {
      
      val word = input.getString(0)
      
      val optCount = counts.get(word)
      if(optCount.isEmpty) {
	counts.put(word,1)
      } else {
	counts.put(word,optCount.get+1)
      }
      
      collector.emit(new Values(word,counts))
    }
    
    override def declareOutputFields(declarer: OutputFieldsDeclarer): Unit = {
      
      declarer.declare(new Fields("word","count"));
    }
    
  }

  def main(args: Array[String]): Unit = {
    println("Hello, world!")
    val builder = new TopologyBuilder
    builder.setSpout("spout", new RandomSentenceSpout, 5)
    builder.setBolt("split", new SplitSentenceBolt, 8).shuffleGrouping("spout")
    builder.setBolt("count", new WordCountBolt, 12).fieldsGrouping("split", new Fields("word"))
    
    val conf = new Config()
    conf.setDebug(true)
    
    if (args != null && args.length > 0) {
      conf.setNumWorkers(3)
      StormSubmitter.submitTopology(args(0), conf, builder.createTopology())
    }
    else {
      conf.setMaxTaskParallelism(3)
      val cluster = new LocalCluster
      cluster.submitTopology("word-count", conf, builder.createTopology())
      Thread.sleep(10000)
      cluster.shutdown()
    }
  }
}
