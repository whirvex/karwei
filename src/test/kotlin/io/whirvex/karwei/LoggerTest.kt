/*
 * the MIT License (MIT)
 *
 * Copyright (c) 2025-2026 Whirvex Software, LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * the above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.whirvex.karwei

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlin.test.*

private object Danger : TaskLogLevel(
    level = 35, /* between Warn and Error */
    name = "DANGER",
)

internal class LoggerTest {

    @Test
    fun levelsExtendTaskLogLevel() {
        val levels = listOf(
            TaskLogLevel.Trace,
            TaskLogLevel.Debug,
            TaskLogLevel.Info,
            TaskLogLevel.Warn,
            TaskLogLevel.Error,
            TaskLogLevel.Fatal,
            TaskLogLevel.Off,
        )

        levels.forEach { assertIs<TaskLogLevel>(it) }
    }

    @Test
    fun levelCompareToUsesLevel() {
        assertTrue { Danger > TaskLogLevel.Warn }
        assertTrue { TaskLogLevel.Warn < Danger }
    }

    @Test
    fun levelEqualsReturnsTrueForSelf() {
        assertTrue { TaskLogLevel.Info == TaskLogLevel.Info }
    }

    @Test
    fun levelEqualsReturnsFalseForOtherTypes() {
        assertFalse { Danger.equals(Danger.level) }
        assertFalse { Danger.equals(Danger.name) }
    }

    @Test
    fun levelEqualsChecksLevel() {
        val level = TaskLogLevel(
            level = Danger.level - 1,
            name = Danger.name,
        )
        assertFalse { Danger == level }
    }

    @Test
    fun levelEqualsIgnoresName() {
        val level = TaskLogLevel(
            level = Danger.level,
            name = Danger.name.reversed(),
        )
        assertTrue { Danger == level }
    }

    @Test
    fun levelHashCodeReturnsLevel() {
        assertEquals(Danger.level, Danger.hashCode())
    }

    @Test
    fun levelToStringReturnsName() {
        assertEquals(Danger.name, Danger.toString())
    }

    @Test
    fun staticTaskLoggerThrowsOnLog(): Unit = runBlocking {
        val taskLogger = task {}
            .taskFlow().toList()
            .first().context.logger
        assertFailsWith<IllegalStateException> {
            taskLogger.trace {}
        }
    }

    @Test
    fun liveTaskLoggerEmitsEventOnLog(): Unit = runBlocking {
        val logEvents = task { taskLogger.trace {} }
            .taskFlow().toList()
            .filterIsInstance<TaskLogEvent>()
        assertTrue { logEvents.isNotEmpty() }
    }

    @Test
    fun logMethodsUseCorrectLevels(): Unit = runBlocking {
        val loggingTask = task {
            taskLogger.trace {}
            taskLogger.debug {}
            taskLogger.info {}
            taskLogger.warn {}
            taskLogger.error {}
            taskLogger.fatal {}
        }

        val logLevels = loggingTask
            .taskFlow().toList()
            .filterIsInstance<TaskLogEvent>()
            .map { it.level }

        assertEquals(logLevels[0], TaskLogLevel.Trace)
        assertEquals(logLevels[1], TaskLogLevel.Debug)
        assertEquals(logLevels[2], TaskLogLevel.Info)
        assertEquals(logLevels[3], TaskLogLevel.Warn)
        assertEquals(logLevels[4], TaskLogLevel.Error)
        assertEquals(logLevels[5], TaskLogLevel.Fatal)
    }

}
