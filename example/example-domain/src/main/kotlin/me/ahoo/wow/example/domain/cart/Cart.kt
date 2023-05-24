/*
 * Copyright [2021-present] [ahoo wang <ahoowang@qq.com> (https://github.com/Ahoo-Wang)].
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.ahoo.wow.example.domain.cart

import me.ahoo.wow.api.annotation.AggregateRoot
import me.ahoo.wow.api.annotation.StaticTenantId
import me.ahoo.wow.example.api.cart.AddCartItem
import me.ahoo.wow.example.api.cart.CartInitialized
import me.ahoo.wow.example.api.cart.CartItem
import me.ahoo.wow.example.api.cart.CartItemAdded
import me.ahoo.wow.example.api.cart.CartItemRemoved
import me.ahoo.wow.example.api.cart.CartQuantityChanged
import me.ahoo.wow.example.api.cart.ChangeQuantity
import me.ahoo.wow.example.api.cart.InitializeCart
import me.ahoo.wow.example.api.cart.RemoveCartItem

const val MAX_CART_ITEM_SIZE = 100

@StaticTenantId
@AggregateRoot
class Cart(private val state: CartState) {

    @Suppress("UnusedParameter")
    fun onCommand(command: InitializeCart): CartInitialized {
        return CartInitialized
    }

    fun onCommand(command: AddCartItem): CartItemAdded {
        require(state.items.size < MAX_CART_ITEM_SIZE) {
            "购物车最多只能添加[$MAX_CART_ITEM_SIZE]个商品."
        }
        val added = CartItem(
            productId = command.productId,
            quantity = command.quantity,
        )
        return CartItemAdded(
            added = added,
        )
    }

    fun onCommand(command: RemoveCartItem): CartItemRemoved {
        return CartItemRemoved(
            productIds = command.productIds,
        )
    }

    fun onCommand(command: ChangeQuantity): CartQuantityChanged {
        return CartQuantityChanged(
            changed = state.items.first {
                it.productId == command.productId
            }.copy(quantity = command.quantity),
        )
    }
}
