/*
 * This file is part of Radical Cobblemon Trainers.
 * Copyright (c) 2026, HDainester, All rights reserved.
 *
 * Radical Cobblemon Trainers is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Radical Cobblemon Trainers is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Radical Cobblemon Trainers. If not, see <http://www.gnu.org/licenses/lgpl>.
 */
package com.gitlab.srcmc.rctmod.api.data;

import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

public enum RCTModDataProviders {
    // EXAMPLE(new CustomDataProvider() {
    //     @Override
    //     public String getName() {
    //         return "Example";
    //     }

    //     @Override
    //     public CompletableFuture<?> run(CachedOutput out) {
    //         return CompletableFuture.runAsync(() -> {
    //             try {
    //                 var packOut = this.packOutput();
    //                 var path = Paths.get(packOut != null ? packOut.getOutputFolder().toString() : "", String.format("assets/%s/example.json", ModCommon.MOD_ID));
    //                 var data = "{}";
    //                 out.writeIfNeeded(path, data.getBytes(), HashCode.fromInt(data.hashCode()));
    //             } catch (IOException e) {
    //                 ModCommon.LOG.error(e.getMessage(), e);
    //             }
    //         });
    //     }
    // })
    ;

    private final CustomDataProvider provider;
    private final RegistrySetBuilder registryBuilder;

    private RCTModDataProviders(CustomDataProvider provider) {
        this(provider, new RegistrySetBuilder()); // no registry objects
    }

    private RCTModDataProviders(CustomDataProvider provider, RegistrySetBuilder registryBuilder) {
        this.provider = provider;
        this.registryBuilder = registryBuilder;
    }

    public CustomDataProvider provider() {
        return this.provider;
    }

    public RegistrySetBuilder registryBuilder() {
        return this.registryBuilder;
    }

    public static abstract class CustomDataProvider implements DataProvider {
        private PackOutput packOutput;

        public PackOutput packOutput() {
            return this.packOutput;
        }

        public CustomDataProvider withOutput(PackOutput value) {
            this.packOutput = value;
            return this;
        }
    }
}
