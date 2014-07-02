package org.fxmisc.easybind;

import java.util.List;
import java.util.function.Supplier;

import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;

/**
 * Starting point for creation of conditional bindings. Encapsulates a boolean
 * condition and provides fluent API to create conditional bindings based on
 * that condition.
 */
public final class When {

    private final ObservableValue<Boolean> condition;

    When(ObservableValue<Boolean> condition) {
        this.condition = condition;
    }

    /**
     * Sets up automatic binding and unbinding of {@code target} to/from
     * {@code source}, based on the changing value of the encapsulated
     * condition. In other words, whenever the encapsulated condition is
     * {@code true}, {@code target} is bound to {@code source}. Whenever the
     * encapsulated condition is {@code false}, {@code target} is unbound.
     * This keeps happening until {@code unsubscribe()} is called on the
     * returned subscription. Unsubscribing the returned subscription may be
     * skipped safely only when the lifetimes of all the encapsulated condition,
     * {@code source} and {@code target} are the same.
     * @param target target of the conditional binding
     * @param source source of the conditional binding
     * @return a subscription that can be used to dispose the conditional
     * binding set up by this method, i.e. to stop observing the encapsulated
     * condition and, if the last observed value of the encapsulated condition
     * was {@code true}, unbind {@code target} from {@code source}.
     */
    public <T> Subscription bind(
            Property<T> target,
            ObservableValue<? extends T> source) {
        return bind(() -> {
            target.bind(source);
            return target::unbind;
        });
    }

    /**
     * Sets up automatic binding and unbinding of {@code target}'s items to/from
     * {@code source}'s items, based on the changing value of the encapsulated
     * condition. In other words, whenever the encapsulated condition is
     * {@code true}, {@code target}'s content is synced with {@code source}.
     * Whenever the encapsulated condition is {@code false}, the sync is
     * interrupted. This keeps happening until {@code unsubscribe()} is called
     * on the returned subscription. Unsubscribing the returned subscription may
     * be skipped safely only when the lifetimes of all the encapsulated
     * condition, {@code source} and {@code target} are the same.
     * @param target target of the conditional binding
     * @param source source of the conditional binding
     * @return a subscription that can be used to dispose the conditional
     * binding set up by this method, i.e. to stop observing the encapsulated
     * condition and, if the last observed value of the encapsulated condition
     * was {@code true}, stop the synchronization {@code target}'s content with
     * {@code source}'s content.
     */
    public <T> Subscription listBind(
            List<? super T> target,
            ObservableList<? extends T> source) {
        return bind(() -> EasyBind.listBind(target, source));
    }

    Subscription bind(Supplier<? extends Subscription> bindFn) {
        return new ConditionalSubscription(condition, bindFn);
    }
}


class ConditionalSubscription implements Subscription {
    private final Supplier<? extends Subscription> bindFn;
    private final ObservableValue<Boolean> condition;
    private final ChangeListener<Boolean> conditionListener = this::conditionChanged;

    private Subscription subscription = null;

    public ConditionalSubscription(
            ObservableValue<Boolean> condition,
            Supplier<? extends Subscription> bindFn) {
        this.condition = condition;
        this.bindFn = bindFn;

        condition.addListener(conditionListener);
        if(condition.getValue()) {
            subscription = bindFn.get();
        }
    }

    private void conditionChanged(ObservableValue<? extends Boolean> cond,
            Boolean wasTrue, Boolean isTrue) {
        if(isTrue) {
            assert subscription == null;
            subscription = bindFn.get();
        } else {
            assert subscription != null;
            subscription.unsubscribe();
            subscription = null;
        }
    }

    @Override
    public void unsubscribe() {
        condition.removeListener(conditionListener);
        if(subscription != null) {
            subscription.unsubscribe();
            subscription = null;
        }
    }
}