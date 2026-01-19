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

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.test.*

internal class FlowTest {

    @Test
    fun taskResultStateIsAccurate() {
        val result = TaskFlowResult<Int>()
        assertFalse { result.computedResult }
        result.value = 0
        assertTrue { result.computedResult }
    }

    @Test
    fun taskResultThrowsOnPrematureGet() {
        val result = TaskFlowResult<Int>()
        val value by result /* delegate should use result.get() */
        assertFailsWith<NoSuchElementException> { value }
    }

    @Test
    fun taskResultThrowsOnDoubleSet() {
        val result = TaskFlowResult<Int>()
        result.value = 0
        assertFailsWith<IllegalStateException> { result.value = 0 }
    }

    @Test
    fun taskResultContainsSetValue() {
        val result = TaskFlowResult<Int>()
        result.value = 123
        assertEquals(123, result.get())
    }

    @Test
    fun taskResultAllowsNullables() {
        val result = TaskFlowResult<Int?>()
        result.value = null
        assertNull(result.get())
    }

    @Test
    fun taskFlowThrowsForComputedResult(): Unit = runBlocking {
        val result = TaskFlowResult<Int>()
        result.value = 0
        assertFailsWith<IllegalArgumentException> {
            task { 0 }.taskFlow(result).collect {}
        }
    }

    @Test
    fun taskFlowAllowsNullResult(): Unit = runBlocking {
        assertDoesNotThrow {
            task { 0 }.taskFlow(result = null).collect {}
        }
    }

    @Test
    fun taskFlowSavesResult(): Unit = runBlocking {
        val result = TaskFlowResult<Int>()
        task { 123 }.taskFlow(result).collect {}
        assertEquals(123, result.get())
    }

    @Test
    fun taskFlowIgnoresSubtasksResults(): Unit = runBlocking {
        val runnable = task {
            task { 123 }.runBlocking()
            456 /* result should contain this, not 123 */
        }

        val result = TaskFlowResult<Int>()
        runnable.taskFlow(result).collect {}
        assertEquals(456, result.get())
    }

    @Test
    fun taskFlowAllowsUnit(): Unit = runBlocking {
        task {}.taskFlow().collect {}
    }

}
