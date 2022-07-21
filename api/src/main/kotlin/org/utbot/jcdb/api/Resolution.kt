package org.utbot.jcdb.api


interface Resolution
interface MethodResolution : Resolution
interface RecordComponentResolution : Resolution
interface FieldResolution : Resolution
interface TypeResolution : Resolution

object Malformed : TypeResolution, FieldResolution, MethodResolution, RecordComponentResolution
object Raw : TypeResolution, FieldResolution, MethodResolution, RecordComponentResolution
