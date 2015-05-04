/*
 * Copyright 2014 Higher Frequency Trading
 *
 * http://www.higherfrequencytrading.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.openhft.chronicle.queue.stateless;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.ExcerptTailer;
import net.openhft.chronicle.queue.stateless.bytes.StatelessRawBytesTailer;
import net.openhft.chronicle.wire.Wire;
import net.openhft.chronicle.wire.WireIn;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * todo : currently work in process
 *
 * Created by Rob Austin
 */
public class StatelessTailer implements ExcerptTailer {

    private final StatelessRawBytesTailer statelessRawBytesTailer;
    private final ChronicleQueue chronicleQueue;
    private final Function<Bytes, Wire> wireFunction;
    private long index = -1;

    public StatelessTailer(ChronicleQueue chronicleQueue,
                           Function<Bytes, Wire> wireFunction,
                           StatelessRawBytesTailer statelessRawBytesTailer) {
        this.wireFunction = wireFunction;
        this.statelessRawBytesTailer = statelessRawBytesTailer;
        this.chronicleQueue = chronicleQueue;
    }

    /**
     * The wire associated with the current index, calling this method moves on the index
     *
     * @return the wire generated by the {@code wireFunction} and populated with the {@code bytes}
     */
    @Override
    public WireIn wire() {
        if (index == -1) {
            index = statelessRawBytesTailer.lastWrittenIndex();
        }

        final Bytes bytes = statelessRawBytesTailer.readExcept(index);
        index++;
        return wireFunction.apply(bytes);
    }

    @Override
    public boolean readDocument(@NotNull Consumer<WireIn> reader) {
        reader.accept(wire());
        return true;
    }

    @Override
    public boolean index(long l) {
        index = l;
        return statelessRawBytesTailer.index(l);
    }

    @NotNull
    @Override
    public ExcerptTailer toStart() {
        index = 0;
        return this;
    }

    @NotNull
    @Override
    public ExcerptTailer toEnd() {
        index = statelessRawBytesTailer.lastWrittenIndex();
        return this;
    }

    @Override
    public ChronicleQueue chronicle() {
        return chronicleQueue;
    }
}
