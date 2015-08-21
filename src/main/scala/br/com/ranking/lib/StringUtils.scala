package br.com.ranking.lib

import java.net.URLEncoder
import java.text.Normalizer
import java.text.Normalizer.Form

import net.liftweb.common.Loggable
import org.apache.commons.lang3.StringEscapeUtils
import org.bson.types.ObjectId

import scala.annotation.tailrec
import scala.util.Random

class StringUtils(in: String) extends Loggable {
  def toSlug: String = toSlug()

  /** Converts given string to a slug.
    *
    * @note Input string will be converted to a "search engine optimization friendly string"
    * using canonical decomposition, lower case, replacing space with "-".
    * For example: "Tĥïŝ ĩš â fůňķŷ Šťŕĭńġ" ==> "this-is-a-funky-string"
    */
  def toSlug(
    trimInner: Boolean = true,
    trimLeft: Boolean = true,
    trimRight: Boolean = true,
    urlEncode: Boolean = true,
    addSalt: Boolean = false,
    maxLength : Int = 256
  ): String = {
    implicit def enrich(in: String) = new StringUtils(in)

    val Invalid = "[^a-zA-Z0-9_-]".r
    val Separator = "-"

    var result = Invalid.replaceAllIn(toNFD.toLowerCase, Separator)

    if (trimInner) result = result.recursiveReplace(Separator + Separator, Separator)
    if (trimLeft)  result = result.trimLeft(Separator)
    if (trimRight) result = result.trimRight(Separator)
    if (urlEncode) result = URLEncoder.encode(result, "UTF-8")

    if (addSalt)   {
      val salt = new ObjectId()

      result = limitString(result, maxLength, salt.toString.length) // adds object id's length to truncate string
      result = "%s-%s".format(result, salt)
    } else {
      result = limitString(result, maxLength)
    }

    result
  }

  def limitString(text: String = in, maxLength: Int = 256, padding: Int = 0): String = {
    val realLength: Int = maxLength - 1 - padding  // - 1 = substring's fault.

    if(text.length > realLength)
      text.substring(0, realLength)
    else
      text
  }

  def recursiveReplace(target: List[String], replacement: String): String = {
    implicit def enrich(in: String) = new StringUtils(in)
    target.foldLeft(in)((sub, tar) => sub.recursiveReplace(tar, replacement))
  }

  def recursiveReplace(target: String, replacement: String): String = {
    @tailrec
    def inner(subject: String): String = subject match {
      case s if replacement.contains(target) =>
        // Avoids infinite recursion
        subject.replace(target, replacement)
      case s if !s.contains(target) =>
        subject
      case _ =>
        inner(subject.replace(target, replacement))
    }

    inner(in)
  }

  /** Remove accent marks from string.
    *
    * @note Using the normalization Form D (NFD) i.e Canonical Decomposition.
    * Gets rid of accents by converting accented characters to regular ones.
    * For example: "Tĥïŝ ĩš â fůňķŷ Šťŕĭńġ" ==> "This is a funky String"
    *
    * @see http://download.oracle.com/javase/6/docs/api/java/text/Normalizer.html
    */
  def toNFD: String = {
    val AccentMarks = "[^\\p{ASCII}]"

    Normalizer
      .normalize(in, Form.NFD)
      .replaceAll(AccentMarks, "")
  }

  /** Return a new string, removing all instances of the given pattern from the beginning of the original string. */
  def trimLeft(pattern: String = " "): String = {
    @tailrec
    def inner(index: Int): String = in.substring(index) match {
      case subString if !subString.startsWith(pattern) => subString
      case _ => inner(index + pattern.length)
    }

    inner(0)
  }

  /** Return a new string, removing all instances of the given pattern from the end of the original string. */
  def trimRight(pattern: String = " "): String = {
    @tailrec
    def inner(index: Int): String = in.substring(0, index) match {
      case subString if !subString.endsWith(pattern) => subString
      case _ => inner(index - pattern.length)
    }

    inner(in.length)
  }

  /** The "Elvis Operator" for FishyStrings. Ignoring both null and empty values. */
  def ?:(value: String): String = {
    // Note: for symbolic methods, "in" is the parameter, not the subject.
    if (value == null || value.trim == "")
      in
    else
      value
  }

  def encode: String = {
    val ascii = StringEscapeUtils.escapeHtml4(in)
    // val bytes = ascii.getBytes("UTF-8")
    // val base64 = Base64.encodeBase64(bytes)
    // val result = new String(base64)
    // result
    ascii
  }

  def decode: String = {
    // val base64 = in.getBytes("UTF-8")
    // val bytes = Base64.decodeBase64(base64)
    // val ascii = new String(bytes)
    // val result = StringEscapeUtils.unescapeHtml4(ascii)
    val result = StringEscapeUtils.unescapeHtml4(in)
    result
  }

  def ? (replace: String) = in.replaceFirst("#", replace)
  def ? (replace: AnyVal) = in.replaceFirst("#", replace.toString)
  def ?* (tuple: (String, String)) = in.replaceAll(tuple._1, tuple._2)

  private def splitStringWithTag(string: String, tag: String) = {
    val openCloseTag = tag.contains("><")
    if (openCloseTag) {
      val tagName = tag.substring(0, tag.indexOf("><")).replaceAll("[\\W]+", "")
      val split = string.split("<" + tagName + "[^>]*>").toList.map(_.replaceAll("</" + tagName + ">", ""))
      if (split.nonEmpty && split.head.equals("")) split.drop(1) else split
    } else {
      val tagName = tag.replaceAll("[\\W]+", "")
      string.split("<" + tagName + "[\\s]*/?>").toList
    }
  }
  def ?<>| (tags: String*): List[String] = {
    def iter(stringList: List[String], tags: List[String]): List[String] = tags match {
      case Nil => stringList
      case t :: ts => iter(stringList.map(str => splitStringWithTag(str, t)).flatten, ts)
    }
    iter(List(in), tags.toList)
  }

  def firstLetterUppercase = {
    in.split(" ").toList.map(s =>
      if (s.length > 1) "%s%s".format(s.substring(0, 1).toUpperCase, s.substring(1).toLowerCase)
      else s.toUpperCase).mkString(" ")
  }

  override def toString: String = in
}

object StringUtil {
  def alphaRandom(limit: Int) = randomString("abcdefghijklmnopqrstuvwxyz")(limit)
  def numericRandom(limit: Int) = randomString("0123456789")(limit)
  def alphanumericRandom(limit: Int) = randomString("abcdefghijklmnopqrstuvwxyz0123456789")(limit)

  // Generate a random string of length n from the given alphabet
  def randomString(alphabet: String)(n: Int): String = Stream.continually(Random.nextInt(alphabet.size)).map(alphabet).take(n).mkString
}