//package br.com.ranking.lib
//
///**
// * ForwardPipeline - reproduces F#'s forward pipeline operator behavior:
// * x |> f |> g => g(f(x))
// * Useful when chaining function calls.
// *
// * See http://blogs.msdn.com/b/rathblog/archive/2008/04/09/learning-f-post-1-forward-pipe.aspx
// */
//
//class ForwardPipeline[A](in: A) {
//  def |>[B](f: A => B) = f(in)
//}
