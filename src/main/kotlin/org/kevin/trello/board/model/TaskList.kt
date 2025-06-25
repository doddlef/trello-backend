package org.kevin.trello.board.model

data class TaskList(
    val listId: String,
    val name: String,
    val position: Int,
    val boardId: String,
    val createdBy: String,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TaskList

        return listId == other.listId
    }

    override fun hashCode(): Int {
        return listId.hashCode()
    }
}
