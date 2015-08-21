//package br.com.ranking.lib
//
//import net.liftweb.common.Logger
//
//class PULogger(in: Logger) {
//  type TLog = (=> AnyRef) => Unit
//  val log: TLog = in.debug(_)
//
//  def time[A](label: String)(block: => A): A = {
//    val t0 = System.nanoTime
//    val result = block
//    val t1 = System.nanoTime
//
//    val delta = t1 - t0
//    log("--- Time spent on '%s': %d ns. ---".format(label, delta))
//
//    result
//  }
//
//  def value[T](text: String)(expr: => T) = {
//    val result = expr
//
//    log("%s = %s".format(text, result.toString))
//
//    result
//  }
//}
