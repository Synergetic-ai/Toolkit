/*
 * Copyright 2018 Dept. Protein Evolution, Max Planck Institute for Developmental Biology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.proteinevolution.jobs.results

import io.circe.JsonObject
import io.circe.syntax._
import play.twirl.api.Html

import scala.collection.mutable.ArrayBuffer

// TODO scrap this

object Common {

  private val color_regex   = """(?:[WYF]+|[LIVM]+|[AST]+|[KR]+|[DE]+|[QN]+|H+|C+|P+|G+)""".r
  private val helix_pattern = """([Hh]+)""".r
  private val sheet_pattern = """([Ee]+)""".r
  private val helix_sheets  = """([Hh]+|[Ee]+)""".r("ss")

  private val uniprotReg    = """([A-Z0-9]{10}|[A-Z0-9]{6})""".r
  private val scopReg       = """([defgh][0-9a-zA-Z\.\_]+)""".r
  private val smartReg      = """(^SM0[0-9]{4})""".r
  private val ncbiCDReg     = """(^[cs]d[0-9]{5})""".r
  private val cogkogReg     = """(^[CK]OG[0-9]{4})""".r
  private val tigrReg       = """(^TIGR[0-9]{5})""".r
  private val prkReg        = """(CHL|MTH|PHA|PLN|PTZ|PRK)[0-9]{5}""".r
  private val mmcifReg      = """(...._[0-9a-zA-Z][0-9a-zA-Z]?[0-9a-zA-Z]?[0-9a-zA-Z]?)""".r
  private val mmcifShortReg = """([0-9]+)""".r
  private val pfamReg       = """(pfam[0-9]+|PF[0-9]+(\.[0-9]+)?)""".r
  private val ncbiReg       = """[A-Z]{2}_?[0-9]+\.?\#?([0-9]+)?|[A-Z]{3}[0-9]{5}?\.[0-9]""".r
  private val ecodReg       = """(ECOD_[0-9]+)_.*""".r

  private val envNrNameReg   = """(env.*|nr.*)""".r
  private val pdbNameReg     = """(pdb.*)""".r
  private val uniprotNameReg = """(uniprot.*)""".r
  private val pfamNameReg    = """(Pfam.*)""".r

  private val pdbBaseLink = "http://www.rcsb.org/pdb/explore/explore.do?structureId="

  private val pdbeBaseLink = "http://www.ebi.ac.uk/pdbe/entry/pdb/"
  private val ncbiBaseLink =
    "http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?SUBMIT=y&db=structure&orig_db=structure&term="
  private val ncbiProteinBaseLink = "https://www.ncbi.nlm.nih.gov/protein/"
  private val scopBaseLink        = "http://scop.berkeley.edu/sid="
  private val pfamBaseLink        = "http://pfam.xfam.org/family/"
  private val cddBaseLink         = "http://www.ncbi.nlm.nih.gov/Structure/cdd/cddsrv.cgi?uid="
  private val uniprotBaseLink     = "http://www.uniprot.org/uniprot/"
  private val smartBaseLink       = "http://smart.embl-heidelberg.de/smart/do_annotation.pl?DOMAIN="
  private val ecodBaseLink        = "http://prodata.swmed.edu/ecod/complete/domain/"

  private val emptyRow = "<tr class=\"blank_row\"><td colspan=\"3\"></td></tr>"

  def SSColorReplace(sequence: String): String =
    helix_sheets.replaceAllIn(
      sequence, { m =>
        m.group("ss") match {
          case helix_pattern(substr) => "<span class=\"ss_e\">" + substr + "</span>"
          case sheet_pattern(substr) => "<span class=\"ss_h\">" + substr + "</span>"
        }
      }
    )

  def colorRegexReplacer(sequence: String): String =
    color_regex.replaceAllIn(sequence, { m =>
      "<span class=\"aa_" + m.toString().charAt(0) + "\">" + m.toString() + "</span>"
    })

  def Highlight(sequence: String): String = {
    "<span class=\"sequenceBold\">" + sequence + "</span>"
  }

  def makeRow(rowClass: String, entries: Array[String]): String = {
    val DOMElement = for (entry <- entries) yield {
      "<td>" + entry.toString + "</td>"
    }
    "<tr class='" + rowClass + "'>" + DOMElement.mkString("") + "</tr>"
  }

  /* GENERATING LINKS FOR HHPRED */

  def getSingleLink(id: String): Html = {
    val db     = identifyDatabase(id)
    val idPfam = id.replaceAll("am.*$||..*", "")
    val idPdb  = id.replaceAll("_.*$", "")
    val link = db match {
      case "scop"    => generateLink(scopBaseLink, id, id)
      case "mmcif"   => generateLink(pdbBaseLink, idPdb, id)
      case "prk"     => generateLink(cddBaseLink, id, id)
      case "ncbicd"  => generateLink(cddBaseLink, id, id)
      case "cogkog"  => generateLink(cddBaseLink, id, id)
      case "tigr"    => generateLink(cddBaseLink, id, id)
      case "pfam"    => generateLink(pfamBaseLink, idPfam + "#tabview=tab0", id)
      case "ncbi"    => generateLink(ncbiProteinBaseLink, id, id)
      case "uniprot" => generateLink(uniprotBaseLink, id, id)
      case "smart"   => generateLink(smartBaseLink, id, id)
      case "ecod"    => val idEcod = id.slice(5, 14); generateLink(ecodBaseLink, idEcod, id)
      case _         => id
    }
    Html(link)
  }

  def getLinks(id: String): Html = {
    val db     = identifyDatabase(id)
    val idNcbi = id.replaceAll("#", ".") + "?report=fasta"
    val idPdb  = id.replaceAll("_.*$", "").toLowerCase
    val idTrimmed = if (id.length > 4) {
      id.slice(1, 5)
    } else {
      id
    }
    val idCDD = id.replaceAll("PF", "pfam").replaceAll("\\..*", "")
    val links = db match {
      case "scop"  => generateLink(scopBaseLink, id, "SCOP") + " | " + generateLink(ncbiBaseLink, idTrimmed, "NCBI")
      case "mmcif" => generateLink(pdbeBaseLink, idPdb, "PDBe")
      case "pfam"  => generateLink(cddBaseLink, idCDD, "CDD")
      case "ncbi"  => generateLink(ncbiProteinBaseLink, idNcbi, "NCBI Fasta")
    }
    Html(links)
  }

  def getSingleLinkDB(db: String, id: String): Html = {
    val idPfam = id.replaceAll("am.*$||..*", "")
    val idPdb  = id.replaceAll("_.*$", "")
    val link = db match {
      case envNrNameReg(_)   => generateLink(ncbiProteinBaseLink, id, id)
      case pdbNameReg(_)     => generateLink(pdbBaseLink, idPdb, id)
      case uniprotNameReg(_) => generateLink(uniprotBaseLink, id, id)
      case pfamNameReg(_)    => generateLink(pfamBaseLink, idPfam + "#tabview=tab0", id)
      case _                 => id
    }
    Html(link)
  }

  def getLinksDB(db: String, id: String): Html = {
    val idNcbi = id.replaceAll("#", ".") + "?report=fasta"
    val idPdb  = id.replaceAll("_.*$", "").toLowerCase
    val idCDD  = id.replaceAll("PF", "pfam").replaceAll("\\..*", "")
    val links = db match {
      case envNrNameReg(_)   => generateLink(ncbiProteinBaseLink, idNcbi, "NCBI Fasta")
      case pdbNameReg(_)     => generateLink(pdbeBaseLink, idPdb, "PDBe")
      case pfamNameReg(_)    => generateLink(cddBaseLink, idCDD, "CDD")
      case uniprotNameReg(_) => generateLink(uniprotBaseLink, id + ".fasta", "UniProt")
    }
    Html(links)
  }

  def displayModellerLink(db: String, proteome: String): Boolean = {
    db == "mmcif70/pdb70" || db == "mmcif30/pdb30" && proteome.isEmpty
  }

  def displayStructLink(id: String): Boolean = {
    val db = identifyDatabase(id)
    db match {
      case "scop"  => true
      case "mmcif" => true
      case "ecod"  => true
      case _       => false
    }
  }

  def getSingleLinkHHBlits(id: String): Html = {
    Html(generateLink(uniprotBaseLink, id, id))
  }

  def getLinksHHBlits(jobID: String, id: String): Html = {
    Html(
      s"<a data-open='templateAlignmentModal' onclick='new TemplateAlignment(${'"'}hhblits${'"'}).get(${'"'}$jobID${'"'},${'"'}$id${'"'})'>Template alignment</a>"
    )
  }

  def getLinksHHpred(jobID: String, id: String): Html = {
    val db    = identifyDatabase(id)
    val links = new ArrayBuffer[String]()
    val idPdb = id.replaceAll("_.*$", "").toLowerCase
    val idTrimmed = if (id.length > 4) {
      id.slice(1, 5)
    } else {
      id
    }
    val idCDD  = id.replaceAll("PF", "pfam")
    val idNcbi = id.replaceAll("#", ".") + "?report=fasta"
    db match {
      case "scop" =>
        links += generateLink(pdbBaseLink, idTrimmed, "PDB")
        links += generateLink(ncbiBaseLink, idTrimmed, "NCBI")
      case "ecod" =>
        val idPdbEcod = id.slice(16, 20)
        links += generateLink(pdbBaseLink, idPdbEcod, "PDB")
      case "mmcif" =>
        links += generateLink(pdbeBaseLink, idPdb, "PDBe")
      case "pfam" =>
        val idCDDPfam = idCDD.replaceAll("\\..*", "")
        links += generateLink(cddBaseLink, idCDDPfam, "CDD")
      case "ncbi" =>
        links += generateLink(ncbiProteinBaseLink, idNcbi, "NCBI Fasta")
      case _ => ()
    }
    Html(links.mkString(" | "))
  }

  def generateLink(baseLink: String, id: String, name: String): String =
    "<a href='" + baseLink + id + "' target='_blank'>" + name + "</a>"

  def identifyDatabase(id: String): String = id match {
    case scopReg(_)       => "scop"
    case mmcifShortReg(_) => "mmcif"
    case mmcifReg(_)      => "mmcif"
    case prkReg(_)        => "prk"
    case ncbiCDReg(_)     => "ncbicd"
    case cogkogReg(_)     => "cogkog"
    case tigrReg(_)       => "tigr"
    case smartReg(_)      => "smart"
    case pfamReg(_, _)    => "pfam"
    case uniprotReg(_)    => "uniprot"
    case ecodReg(_)       => "ecod"
    case ncbiReg(_)       => "ncbi"
    case _: String        => ""
  }

  def percentage(str: String): String = (str.toDouble * 100).toInt.toString + "%"

  def calculatePercentage(num1_ : Int, num2_ : Int): String =
    ((num1_.toDouble / num2_.toDouble) * 100).toInt.toString + "%"

  def wrapSequence(seq: String, num: Int): String = {
    (0 to seq.length)
      .filter(_ % num == 0)
      .map {
        case x if x + num < seq.length => makeRow("sequence", Array("", seq.slice(x, x + num)))
        case x                         => makeRow("sequence", Array("", seq.substring(x)))
      }
      .mkString("")
  }

  def getCheckbox(num: Int): String = {
    "<div class=\"nowrap\"><input type=\"checkbox\" data-id=\"" + num + "\" value=\"" + num +
    "\" name=\"alignment_elem\" class=\"checkbox\"><a onclick=\"Toolkit.resultView.scrollToHit(" + num + ")\">" +
    num + "</a></div>"
  }

  def getAddScrollLink(num: Int): String = {
    "<a onclick=\"Toolkit.resultView.scrollToHit(" + num + ")\">" + num + "</a>"
  }

  def addBreak(description: String): String = {
    description.replaceAll("(\\S{40})", "$1</br>")
  }

  def addBreakHHpred(description: String): String = {
    val index = description.indexOfSlice("; Related PDB entries")
    if (index > 1)
      description.slice(0, index).replaceAll("(\\S{40})", "$1</br>")
    else
      description.replaceAll("(\\S{40})", "$1</br>")
  }

  def insertMatch(seq: String, length: Int, hitArr: List[Int]): String = {
    val inserted = for (starPos <- hitArr) yield {
      seq.slice(0, starPos) + "<span class=\"patternMatch\">" + seq.slice(starPos, starPos + length) + "</span>" + seq
        .substring(starPos + length)
    }
    inserted.mkString("")
  }

  def clustal(alignment: AlignmentResult, breakAfter: Int, color: Boolean): JsonObject = {
    JsonObject(
      "breakAfter" -> breakAfter.asJson,
      "alignment" -> alignment.alignment.map { elem =>
        JsonObject(
          "num"       -> elem.num.asJson,
          "accession" -> elem.accession.take(20).asJson,
          "seq" -> {
            if (color) colorRegexReplacer(elem.seq)
            else elem.seq
          }.asJson
        )
      }.asJson
    )
  }

  def hmmerHitWrapped(hit: HmmerHSP, charCount: Int, breakAfter: Int, beginQuery: Int, beginTemplate: Int): String = {
    if (charCount >= hit.hit_len) {
      ""
    } else {
      val query       = hit.query_seq.slice(charCount, Math.min(charCount + breakAfter, hit.query_seq.length))
      val midline     = hit.midline.slice(charCount, Math.min(charCount + breakAfter, hit.midline.length))
      val template    = hit.hit_seq.slice(charCount, Math.min(charCount + breakAfter, hit.hit_seq.length))
      val queryEnd    = lengthWithoutDashDots(query)
      val templateEnd = lengthWithoutDashDots(template)
      if (beginQuery == beginQuery + queryEnd) {
        ""
      } else {
        makeRow("sequence", Array("", "Q " + (beginQuery + 1), query + "   " + (beginQuery + queryEnd))) +
        makeRow("sequence", Array("", "", midline)) +
        makeRow("sequence", Array("", "T " + (beginTemplate + 1), template + "   " + (beginTemplate + templateEnd))) +
        emptyRow + emptyRow +
        hmmerHitWrapped(hit, charCount + breakAfter, breakAfter, beginQuery + queryEnd, beginTemplate + templateEnd)
      }
    }
  }

  def psiblastHitWrapped(
      hit: PSIBlastHSP,
      charCount: Int,
      breakAfter: Int,
      beginQuery: Int,
      beginTemplate: Int
  ): String = {
    if (charCount >= hit.hit_len) {
      ""
    } else {
      val query       = hit.query_seq.slice(charCount, Math.min(charCount + breakAfter, hit.query_seq.length))
      val midline     = hit.midLine.slice(charCount, Math.min(charCount + breakAfter, hit.midLine.length))
      val template    = hit.hit_seq.slice(charCount, Math.min(charCount + breakAfter, hit.hit_seq.length))
      val queryEnd    = lengthWithoutDashDots(query)
      val templateEnd = lengthWithoutDashDots(template)
      if (beginQuery == beginQuery + queryEnd) {
        ""
      } else {
        makeRow("sequence", Array("", "Q " + beginQuery, query + "   " + (beginQuery + queryEnd - 1))) +
        makeRow("sequence", Array("", "", midline)) +
        makeRow("sequence", Array("", "T " + beginTemplate, template + "   " + (beginTemplate + templateEnd - 1))) +
        emptyRow + emptyRow +
        psiblastHitWrapped(hit, charCount + breakAfter, breakAfter, beginQuery + queryEnd, beginTemplate + templateEnd)
      }
    }
  }

  def lengthWithoutDashDots(str: String): Int = {
    str.length - str.count(char => char == '-') - str.count(char => char == '.')
  }

  def hhblitsHitWrapped(
      hit: HHBlitsHSP,
      charCount: Int,
      breakAfter: Int,
      beginQuery: Int,
      beginTemplate: Int
  ): String = {
    if (charCount >= hit.length) {
      ""
    } else {
      val query = hit.query.seq.slice(charCount, Math.min(charCount + breakAfter, hit.query.seq.length))
      val queryCons =
        hit.query.consensus.slice(charCount, Math.min(charCount + breakAfter, hit.query.consensus.length))
      val midline = hit.agree.slice(charCount, Math.min(charCount + breakAfter, hit.agree.length))
      val templateCons =
        hit.template.consensus.slice(charCount, Math.min(charCount + breakAfter, hit.template.consensus.length))
      val template =
        hit.template.seq.slice(charCount, Math.min(charCount + breakAfter, hit.template.seq.length))
      val queryEnd    = lengthWithoutDashDots(query)
      val templateEnd = lengthWithoutDashDots(template)
      if (beginQuery == beginQuery + queryEnd) {
        ""
      } else {
        makeRow(
          "sequence",
          Array("", "Q " + beginQuery, query + "  " + (beginQuery + queryEnd - 1) + " (" + hit.query.ref + ")")
        ) +
        makeRow("sequence", Array("", "", queryCons)) +
        makeRow("sequence", Array("", "", midline)) +
        makeRow("sequence", Array("", "", templateCons)) +
        makeRow(
          "sequence",
          Array(
            "",
            "T " + beginTemplate,
            template + "  " + (beginTemplate + templateEnd - 1) + " (" + hit.template.ref + ")"
          )
        ) +
        emptyRow + emptyRow +
        hhblitsHitWrapped(hit, charCount + breakAfter, breakAfter, beginQuery + queryEnd, beginTemplate + templateEnd)
      }
    }
  }

  def hhpredHitWrapped(
      hit: HHPredHSP,
      charCount: Int,
      breakAfter: Int,
      beginQuery: Int,
      beginTemplate: Int,
      color: Boolean
  ): String = {
    if (charCount >= hit.length) {
      ""
    } else {
      val querySSDSSP = hit.query.ss_dssp.map(s => s.slice(charCount, Math.min(charCount + breakAfter, s.length)))
      val querySSPRED = hit.query.ss_pred.map(s => s.slice(charCount, Math.min(charCount + breakAfter, s.length)))
      val query       = hit.query.seq.slice(charCount, Math.min(charCount + breakAfter, hit.query.seq.length))
      val queryCons =
        hit.query.consensus.slice(charCount, Math.min(charCount + breakAfter, hit.query.consensus.length))
      val midline = hit.agree.slice(charCount, Math.min(charCount + breakAfter, hit.agree.length))
      val templateCons =
        hit.template.consensus.slice(charCount, Math.min(charCount + breakAfter, hit.template.consensus.length))
      val template = hit.template.seq.slice(charCount, Math.min(charCount + breakAfter, hit.template.seq.length))
      val templateSSDSSP =
        hit.template.ss_dssp.slice(charCount, Math.min(charCount + breakAfter, hit.template.ss_dssp.length))
      val templateSSPRED =
        hit.template.ss_pred.slice(charCount, Math.min(charCount + breakAfter, hit.template.ss_pred.length))
      val confidence  = hit.confidence.slice(charCount, Math.min(charCount + breakAfter, hit.confidence.length))
      val queryEnd    = lengthWithoutDashDots(query)
      val templateEnd = lengthWithoutDashDots(template)

      if (beginQuery == beginQuery + queryEnd) {
        ""
      } else {
        var html = ""
        if (querySSPRED.get.nonEmpty) {
          html += makeRow("sequence", Array("", "Q ss_pred", "", Common.SSColorReplace(querySSPRED.get)))
        }
        if (querySSDSSP.get.nonEmpty) {
          html += makeRow("sequence", Array("", "Q ss_dssp", "", Common.SSColorReplace(querySSDSSP.get)))
        }
        html += makeRow(
          "sequence",
          Array(
            "",
            "Q " + hit.query.accession,
            beginQuery.toString,
            s"${if (color) colorRegexReplacer(query) else query}  ${beginQuery + queryEnd - 1} (${hit.query.ref})"
          )
        )
        html += makeRow(
          "sequence",
          Array(
            "",
            "Q Consensus ",
            beginQuery.toString,
            queryCons + "  " + (beginQuery + queryEnd - 1) + " (" + hit.query.ref + ")"
          )
        )
        html += makeRow("sequence", Array("", "", "", midline))
        html += makeRow(
          "sequence",
          Array(
            "",
            "T Consensus ",
            beginTemplate.toString,
            "%s  %d (%d)".format(templateCons, beginTemplate + templateEnd - 1, hit.template.ref)
          )
        )
        html += makeRow(
          "sequence",
          Array(
            "",
            "T " + hit.template.accession,
            beginTemplate.toString,
            s"${if (color) colorRegexReplacer(template) else template}  ${beginTemplate + templateEnd - 1} (${hit.template.ref})"
          )
        )
        if (!templateSSDSSP.isEmpty) {
          html += makeRow("sequence", Array("", "T ss_dssp", "", Common.SSColorReplace(templateSSDSSP)))
        }
        if (!templateSSPRED.isEmpty) {
          html += makeRow("sequence", Array("", "T ss_pred", "", Common.SSColorReplace(templateSSPRED)))
        }
        if (!confidence.isEmpty) {
          html += makeRow("sequence", Array("", "Confidence", "", confidence))
        }
        html += emptyRow + emptyRow
        html + hhpredHitWrapped(
          hit,
          charCount + breakAfter,
          breakAfter,
          beginQuery + queryEnd,
          beginTemplate + templateEnd,
          color
        )
      }
    }
  }

  def hhompHitWrapped(
      hit: HHompHSP,
      charCount: Int,
      breakAfter: Int,
      beginQuery: Int,
      beginTemplate: Int,
      color: Boolean
  ): String = {
    if (charCount >= hit.length) {
      ""
    } else {
      val querySSCONF = hit.query.ss_conf.slice(charCount, Math.min(charCount + breakAfter, hit.query.ss_conf.length))
      val querySSDSSP = hit.query.ss_dssp.slice(charCount, Math.min(charCount + breakAfter, hit.query.ss_dssp.length))
      val querySSPRED = hit.query.ss_pred.slice(charCount, Math.min(charCount + breakAfter, hit.query.ss_pred.length))
      val query       = hit.query.seq.slice(charCount, Math.min(charCount + breakAfter, hit.query.seq.length))
      val queryCons =
        hit.query.consensus.slice(charCount, Math.min(charCount + breakAfter, hit.query.consensus.length))
      val midline = hit.agree.slice(charCount, Math.min(charCount + breakAfter, hit.agree.length))
      val templateCons =
        hit.template.consensus.slice(charCount, Math.min(charCount + breakAfter, hit.template.consensus.length))
      val template = hit.template.seq.slice(charCount, Math.min(charCount + breakAfter, hit.template.seq.length))
      val templateSSDSSP =
        hit.template.ss_dssp.slice(charCount, Math.min(charCount + breakAfter, hit.template.ss_dssp.length))
      val templateSSPRED =
        hit.template.ss_pred.slice(charCount, Math.min(charCount + breakAfter, hit.template.ss_pred.length))
      val templateSSCONF =
        hit.template.ss_conf.slice(charCount, Math.min(charCount + breakAfter, hit.template.ss_conf.length))
      val templateBBPRED =
        hit.template.bb_pred.slice(charCount, Math.min(charCount + breakAfter, hit.template.bb_pred.length))
      val templateBBCONF =
        hit.template.bb_conf.slice(charCount, Math.min(charCount + breakAfter, hit.template.bb_conf.length))

      val queryEnd    = lengthWithoutDashDots(query)
      val templateEnd = lengthWithoutDashDots(template)

      if (beginQuery == beginQuery + queryEnd) {
        ""
      } else {
        var html = ""
        if (!querySSCONF.isEmpty) {
          html += makeRow("sequence", Array("", "Q ss_conf", "", querySSCONF))
        }
        if (!querySSPRED.isEmpty) {
          html += makeRow("sequence", Array("", "Q ss_pred", "", Common.SSColorReplace(querySSPRED)))
        }
        if (!querySSDSSP.isEmpty) {
          html += makeRow("sequence", Array("", "Q ss_dssp", "", Common.SSColorReplace(querySSDSSP)))
        }
        html += makeRow(
          "sequence",
          Array(
            "",
            "Q " + hit.query.accession,
            beginQuery.toString,
            { if (color) colorRegexReplacer(query) else query } + "  " + (beginQuery + queryEnd - 1) + " (" + hit.query.ref + ")"
          )
        )
        html += makeRow(
          "sequence",
          Array(
            "",
            "Q Consensus ",
            beginQuery.toString,
            queryCons + "  " + (beginQuery + queryEnd - 1) + " (" + hit.query.ref + ")"
          )
        )
        html += makeRow("sequence", Array("", "", "", midline))
        html += makeRow(
          "sequence",
          Array(
            "",
            "T Consensus ",
            beginTemplate.toString,
            templateCons + "  " + (beginTemplate + templateEnd - 1) + " (" + hit.template.ref + ")"
          )
        )
        html += makeRow(
          "sequence",
          Array(
            "",
            "T " + hit.template.accession,
            beginTemplate.toString,
            s"${if (color) colorRegexReplacer(template)
            else template}  ${beginTemplate + templateEnd - 1} (${hit.template.ref})"
          )
        )
        if (!templateSSDSSP.isEmpty) {
          html += makeRow("sequence", Array("", "T ss_dssp", "", Common.SSColorReplace(templateSSDSSP)))
        }
        if (!templateSSPRED.isEmpty) {
          html += makeRow("sequence", Array("", "T ss_pred", "", Common.SSColorReplace(templateSSPRED)))
        }
        if (!templateSSCONF.isEmpty) {
          html += makeRow("sequence", Array("", "T ss_conf", "", templateSSCONF))
        }
        if (!templateBBPRED.isEmpty) {
          html += makeRow("sequence", Array("", "T bb_pred", "", templateBBPRED))
        }
        if (!templateBBCONF.isEmpty) {
          html += makeRow("sequence", Array("", "T bb_conf", "", templateBBCONF))
        }

        html += emptyRow + emptyRow

        html + hhompHitWrapped(
          hit,
          charCount + breakAfter,
          breakAfter,
          beginQuery + queryEnd,
          beginTemplate + templateEnd,
          color
        )
      }
    }
  }
}
