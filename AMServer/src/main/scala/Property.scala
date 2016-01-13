object Property {
  import Server._
  private var nodeCount = 0
  private var interval = 100
  def setNodeCount(n:Int) ={
    nodeCount = n
    server ! StartNodes
  }

  def getNodeCount = nodeCount

  def setInterval(t:Int) ={
    interval = t
    server ! StartNodes
  }
  def getInterval = interval
}
