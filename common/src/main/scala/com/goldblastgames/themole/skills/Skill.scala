package com.goldblastgames.themole.skills

object Skills extends Enumeration {
  type Skill = Value
  val Subterfuge,
    InformationGathering,
    Wetwork,
    Sabotage,
    Sexitude,
    Stoicism = Value
  val skillPairs: Map[Skill, Skill] = Map(
    Subterfuge -> InformationGathering,
    InformationGathering -> Subterfuge,
    Wetwork -> Sabotage,
    Sabotage -> Wetwork,
    Sexitude -> Stoicism,
    Stoicism -> Sexitude
    )
}
