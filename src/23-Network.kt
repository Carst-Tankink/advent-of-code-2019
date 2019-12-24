import computer.Machine
import computer.State

data class Node(val computer: Machine, val address: Long) {
    fun run(queue: Map<Long, List<Long>>): Pair<Node, Map<Long, List<Long>>> {
        return when (computer.state) {
            State.Halt -> Pair(this, queue)
            State.Running -> Pair(copy(computer = computer.run()), queue)
            State.Input -> {
                val forAddress = queue[address]
                val (newNode, inbox) = if (forAddress == null || forAddress.isEmpty()) {
                    val after = computer.input(-1)
                    Pair(copy(computer = after), emptyList())
                } else {
                    val x = forAddress[0]
                    val y = forAddress[1]
                    val newInbox = forAddress.drop(2)
                    Pair(copy(computer = computer.input(x).input(y)), newInbox)
                }
                val newQueue = (queue - address) + Pair(address, inbox)
                Pair(newNode, newQueue)
            }
            State.Output -> {
                val receivingAddress = computer.output
                val withX = computer.cont()
                val queueForAddress = queue[receivingAddress] ?: emptyList()

                val x = withX.output
                val withY = withX.cont()

                val y = withY.output

                println("Packet adress $receivingAddress, ($x, $y)")
                val newQueues = (queue - receivingAddress) + Pair(receivingAddress, queueForAddress + listOf(x, y))
                Pair(copy(computer = withY.cont()), newQueues)
            }
        }
    }
}

fun main() {
    val program = Machine.parseProgram("resources/23-input")

    val started = Machine(program).run()
    val nodes: List<Node> = 0L.rangeTo(49)
        .map { address -> Node(started.input(address).input(-1), address) }


    val firstAt255 = runNetwork(nodes)

    println("First packet: $firstAt255")
}

fun runNetwork(nodes: List<Node>): Long {
    tailrec fun rec(nodesRec: List<Node>, queues: Map<Long, List<Long>>): Long {
        val isNetworkIdle =
            nodesRec.all { it.computer.state == State.Input } && queues.filterNot { it.key == 255L }.values.all { it.isEmpty() }
        return if (isNetworkIdle) (queues[255] ?: error(""))[1] else {
            val toRun = nodesRec[0]
            val (newNode, newQueues) = toRun.run(queues)
            rec(nodesRec.drop(1) + newNode, newQueues)
        }
    }

    return rec(nodes, emptyMap())

}
