package `in`.kiranrao.atomicdiffutil

import `in`.kiranrao.atomicdiffutil.ItemDiffRecord.*
import org.junit.Assert.assertEquals
import org.junit.Test

class AtomicDiffUtilTest {

    private val playerItemCallback = PlayerItemCallback()

    private fun calculateTestDiff(
        oldItems: List<Player>,
        newItems: List<Player>
    ): AtomicDiffResult<Player> {
        return calculateAtomicDiff(oldItems, newItems, playerItemCallback)
    }

    @Test
    fun `When only one item changed then result is correct`() {
        val atomicDiffResult = calculateTestDiff(
            listOf(player1, player2, player3, player4),
            listOf(player1, player2, player3.copy(score = 350), player4)
        )

        val changeRecords = atomicDiffResult.changeRecords
        assertEquals(1, changeRecords.size)
        assertEquals(Changed(player3, player3.copy(score = 350), 2, 2, null), changeRecords[0])

        assertEquals(true, atomicDiffResult.removalRecords.isEmpty())
        assertEquals(true, atomicDiffResult.insertionRecords.isEmpty())
    }

    @Test
    fun `When consecutive items changed then result is correct`() {
        val changedPlayer2 = player2.copy(score = 21)
        val changedPlayer3 = player3.copy(score = 42)

        val atomicDiffResult = calculateTestDiff(
            listOf(player1, player2, player3, player4),
            listOf(player1, changedPlayer2, changedPlayer3, player4)
        )

        val changeRecords = atomicDiffResult.changeRecords

        assertEquals(2, changeRecords.size)
        assertEquals(Changed(player2, changedPlayer2, 1, 1, null), changeRecords[0])
        assertEquals(Changed(player3, changedPlayer3, 2, 2, null), changeRecords[1])

        assertEquals(true, atomicDiffResult.removalRecords.isEmpty())
        assertEquals(true, atomicDiffResult.insertionRecords.isEmpty())
    }


    @Test
    fun `When non consecutive items changed then last change is dispatched first`() {
        val changedPlayer2 = player2.copy(score = 21)
        val changedPlayer4 = player4.copy(score = 42)

        val atomicDiffResult = calculateTestDiff(
            listOf(player1, player2, player3, player4),
            listOf(player1, changedPlayer2, player3, changedPlayer4)
        )

        val changeRecords = atomicDiffResult.changeRecords

        assertEquals(2, changeRecords.size)
        assertEquals(Changed(player2, changedPlayer2, 1, 1, null), changeRecords[1])
        assertEquals(Changed(player4, changedPlayer4, 3, 3, null), changeRecords[0])

        assertEquals(true, atomicDiffResult.removalRecords.isEmpty())
        assertEquals(true, atomicDiffResult.insertionRecords.isEmpty())
    }

    @Test
    fun `When disjoint sets of consecutive items changed then last set is dispatched first`() {
        val changedPlayer1 = player1.copy(score = 101)
        val changedPlayer2 = player2.copy(score = 202)
        val changedPlayer4 = player4.copy(score = 404)
        val changedPlayer5 = player5.copy(score = 505)

        val atomicDiffResult = calculateTestDiff(
            listOf(player1, player2, player3, player4, player5),
            listOf(changedPlayer1, changedPlayer2, player3, changedPlayer4, changedPlayer5)
        )

        val changeRecords = atomicDiffResult.changeRecords

        assertEquals(4, changeRecords.size)
        assertEquals(Changed(player4, changedPlayer4, 3, 3, null), changeRecords[0])
        assertEquals(Changed(player5, changedPlayer5, 4, 4, null), changeRecords[1])
        assertEquals(Changed(player1, changedPlayer1, 0, 0, null), changeRecords[2])
        assertEquals(Changed(player2, changedPlayer2, 1, 1, null), changeRecords[3])

        assertEquals(true, atomicDiffResult.removalRecords.isEmpty())
        assertEquals(true, atomicDiffResult.insertionRecords.isEmpty())

    }

    @Test
    fun `When only one item removed then result is correct`() {
        val atomicDiffResult = calculateTestDiff(
            listOf(player1, player2, player3, player4),
            listOf(player1, player2, player4)
        )

        val removalRecords = atomicDiffResult.removalRecords

        assertEquals(1, removalRecords.size)
        assertEquals(Removed(player3, 2), removalRecords[0])

        assertEquals(true, atomicDiffResult.changeRecords.isEmpty())
        assertEquals(true, atomicDiffResult.insertionRecords.isEmpty())
    }

    @Test
    fun `When consecutive items removed then result is correct`() {
        val atomicDiffResult = calculateTestDiff(
            listOf(player1, player2, player3, player4),
            listOf(player1, player2)
        )

        val removalRecords = atomicDiffResult.removalRecords

        assertEquals(2, removalRecords.size)
        assertEquals(Removed(player3, 2), removalRecords[0])
        assertEquals(Removed(player4, 3), removalRecords[1])

        assertEquals(true, atomicDiffResult.changeRecords.isEmpty())
        assertEquals(true, atomicDiffResult.insertionRecords.isEmpty())
    }

    @Test
    fun `When non consecutive items removed then result is correct`() {
        val atomicDiffResult = calculateTestDiff(
            listOf(player1, player2, player3, player4),
            listOf(player1, player3)
        )

        val removalRecords = atomicDiffResult.removalRecords

        assertEquals(2, removalRecords.size)
        assertEquals(Removed(player2, 1), removalRecords[0])
        assertEquals(Removed(player4, 3), removalRecords[1])

        assertEquals(true, atomicDiffResult.changeRecords.isEmpty())
        assertEquals(true, atomicDiffResult.insertionRecords.isEmpty())
    }


    @Test
    fun `When only one item inserted then result is correct`() {
        val atomicDiffResult = calculateTestDiff(
            listOf(player1, player2, player3, player4),
            listOf(player1, player2, player5, player3, player4)
        )

        val insertionRecords = atomicDiffResult.insertionRecords

        assertEquals(1, insertionRecords.size)
        assertEquals(Inserted(player5, 2), insertionRecords[0])

        assertEquals(true, atomicDiffResult.changeRecords.isEmpty())
        assertEquals(true, atomicDiffResult.removalRecords.isEmpty())
    }

    @Test
    fun `When consecutive items inserted then result is correct`() {
        val atomicDiffResult = calculateTestDiff(
            listOf(player1, player2, player3),
            listOf(player4, player5, player1, player2, player3)
        )

        val insertionRecords = atomicDiffResult.insertionRecords

        assertEquals(2, insertionRecords.size)
        assertEquals(Inserted(player4, 0), insertionRecords[0])
        assertEquals(Inserted(player5, 1), insertionRecords[1])

        assertEquals(true, atomicDiffResult.changeRecords.isEmpty())
        assertEquals(true, atomicDiffResult.removalRecords.isEmpty())
    }

    @Test
    fun `When non consecutive items inserted then result is correct`() {
        val atomicDiffResult = calculateTestDiff(
            listOf(player1, player2, player3),
            listOf(player1, player4, player2, player5, player3)
        )

        val insertionRecords = atomicDiffResult.insertionRecords

        assertEquals(2, insertionRecords.size)
        assertEquals(Inserted(player4, 1), insertionRecords[0])
        assertEquals(Inserted(player5, 3), insertionRecords[1])

        assertEquals(true, atomicDiffResult.changeRecords.isEmpty())
        assertEquals(true, atomicDiffResult.removalRecords.isEmpty())
    }

    @Test
    fun `When one change and one removal then result is correct`() {
        val changedPlayer4 = player4.copy(score = 404)

        val atomicDiffResult = calculateTestDiff(
            listOf(player1, player2, player3, player4),
            listOf(player1, player2, changedPlayer4)
        )

        val removalRecords = atomicDiffResult.removalRecords
        val changeRecords = atomicDiffResult.changeRecords

        assertEquals(1, removalRecords.size)
        assertEquals(Removed(player3, 2), removalRecords[0])

        assertEquals(1, changeRecords.size)
        assertEquals(Changed(player4, changedPlayer4, 3, 2, null), changeRecords[0])

        assertEquals(true, atomicDiffResult.insertionRecords.isEmpty())
    }

    @Test
    fun `When one change and one insertion then result is correct`() {
        val changedPlayer2 = player2.copy(score = 202)

        val atomicDiffResult = calculateTestDiff(
            listOf(player1, player2, player3, player4),
            listOf(player1, player5, changedPlayer2, player3, player4)
        )

        val insertionRecords = atomicDiffResult.insertionRecords
        val changeRecords = atomicDiffResult.changeRecords

        assertEquals(1, insertionRecords.size)
        assertEquals(Inserted(player5, 1), insertionRecords[0])

        assertEquals(1, changeRecords.size)
        assertEquals(Changed(player2, changedPlayer2, 1, 2, null), changeRecords[0])

        assertEquals(true, atomicDiffResult.removalRecords.isEmpty())
    }

    @Test
    fun `When one insertion and one removal then result is correct`() {
        val atomicDiffResult = calculateTestDiff(
            listOf(player1, player2, player3, player4),
            listOf(player1, player2, player4, player5)
        )

        val insertionRecords = atomicDiffResult.insertionRecords
        val removalRecords = atomicDiffResult.removalRecords

        assertEquals(1, insertionRecords.size)
        assertEquals(Inserted(player5, 3), insertionRecords[0])

        assertEquals(1, removalRecords.size)
        assertEquals(Removed(player3, 2), removalRecords[0])

        assertEquals(true, atomicDiffResult.changeRecords.isEmpty())
    }

    @Test
    fun `When mix of insertion, removals and changes then result is correct`() {
        val changedPlayer1 = player1.copy(score = 101)
        val changedPlayer5 = player5.copy(score = 505)

        val atomicDiffResult = calculateTestDiff(
            listOf(player1, player2, player3, player4, player5),
            listOf(changedPlayer1, player3, player6, changedPlayer5, player7)
        )

        val insertionRecords = atomicDiffResult.insertionRecords
        val removalRecords = atomicDiffResult.removalRecords
        val changeRecords = atomicDiffResult.changeRecords

        assertEquals(2, changeRecords.size)
        assertEquals(Changed(player5, changedPlayer5, 4, 3, null), changeRecords[0])
        assertEquals(Changed(player1, changedPlayer1, 0, 0, null), changeRecords[1])


        assertEquals(2, insertionRecords.size)
        assertEquals(Inserted(player6, 2), insertionRecords[0])
        assertEquals(Inserted(player7, 4), insertionRecords[1])

        assertEquals(2, removalRecords.size)
        assertEquals(Removed(player2, 1), removalRecords[0])
        assertEquals(Removed(player4, 3), removalRecords[1])
    }
}