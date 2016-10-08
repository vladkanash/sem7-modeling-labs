package com.bsuir.modeling.lab3.chain.element;

import java.util.Random;

/**
 * Created by vladkanash on 6.10.16.
 */
public class BlockingGeneratorChainElement extends MarkovChainElement {

    private final double probability;
    private final Random random = new Random();

    public final static int STATE_BLOCKED = 1;
    public final static int STATE_NOT_BLOCKED = 0;

    public BlockingGeneratorChainElement(String name, double probability) {
        super(name, STATE_NOT_BLOCKED);

        if (probability < 0 || probability > 1) {
            throw new IllegalArgumentException
                    ("Probability value must be between 0 and 1, actual is: " + probability);
        }

        this.probability = probability;
    }

    @Override
    public void addTask() {
        throw new UnsupportedOperationException("You cannot add tasks to generator element");
    }

    @Override
    public void changeState() {
        final MarkovChainElement next = getNext();
        if (next == null) {
            return;
        }
        state = next.isBlocked() ? STATE_BLOCKED : STATE_NOT_BLOCKED;
        if (!next.isBlocked() && random.nextDouble() >= probability) {
            next.addTask();
        }
    }
}
