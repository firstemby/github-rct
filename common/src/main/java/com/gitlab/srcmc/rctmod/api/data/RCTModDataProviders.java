/*
 * This file is part of Radical Cobblemon Trainers.
 * Copyright (c) 2026, HDainester, All rights reserved.
 * 
 * Radical Cobblemon Trainers can be redistributed and/or modified under the terms of the
 * Mod Community License (MCOML).
 * 
 * In short, you may share or modify the source code as you wish, as long as the
 * work stays under this license, and if you have modified the code, you must:
 * 
 *   (a) state your changes if you don't use an SVC like Git
 *   (b) list the original authors (as listed in the copyright at the top of
 *       this license)
 * 
 * When sharing binaries, you must adhere to a few more rules. Either:
 * 
 *   (a) your derivative work differs sufficiently from the original or
 *   (b) you have received written permission from all Project Owners
 * 
 * Radical Cobblemon Trainers is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the MCOML for more details.
 * 
 * You should have received a copy of the MCOML along with Radical Cobblemon Trainers.
 * If not, see <https://gitlab.com/srcmc/mcoml/-/blob/main/v1/license.txt>.
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
