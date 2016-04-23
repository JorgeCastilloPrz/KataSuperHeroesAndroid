/*
 * Copyright (C) 2015 Karumi.
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

package com.karumi.katasuperheroes;

import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.karumi.katasuperheroes.di.MainComponent;
import com.karumi.katasuperheroes.di.MainModule;
import com.karumi.katasuperheroes.matchers.RecyclerViewItemsCountMatcher;
import com.karumi.katasuperheroes.model.SuperHero;
import com.karumi.katasuperheroes.model.SuperHeroesRepository;
import com.karumi.katasuperheroes.ui.view.MainActivity;
import com.karumi.katasuperheroes.ui.view.SuperHeroDetailActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.Collections;

import it.cosenonjaviste.daggermock.DaggerMockRule;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainActivityTest {

    public static final int ANY_COUNT = 10;
    public static final String PREMIUM_SUPER_HERO_BASE_NAME = "Goku ";
    @Rule public DaggerMockRule<MainComponent> daggerRule =
            new DaggerMockRule<>(MainComponent.class, new MainModule()).set(
                    new DaggerMockRule.ComponentSetter<MainComponent>() {
                        @Override
                        public void setComponent(MainComponent component) {
                            SuperHeroesApplication app =
                                    (SuperHeroesApplication) InstrumentationRegistry.getInstrumentation()
                                            .getTargetContext()
                                            .getApplicationContext();
                            app.setComponent(component);
                        }
                    });

    @Rule public IntentsTestRule<MainActivity> activityRule =
            new IntentsTestRule<>(MainActivity.class, true, false);

    @Mock SuperHeroesRepository repository;

    @Test
    public void showsEmptyCaseIfThereAreNoSuperHeroes() {
        givenThereAreNoSuperHeroes();

        startActivity();

        onView(withText("¯\\_(ツ)_/¯")).check(matches(isDisplayed()));
    }

    @Test
    public void doesNotShowEmptyCaseIfThereAreSuperHeroes() {
        givenThereAreSomeSuperHeroes(ANY_COUNT);

        startActivity();

        onView(withText("¯\\_(ツ)_/¯")).check(matches(not(isDisplayed())));
    }

    @Test
    public void listShowsCorrectNumberOfHeroes() {
        givenThereAreSomeSuperHeroes(ANY_COUNT);

        startActivity();

        onView(withId(R.id.recycler_view)).check(matches(new RecyclerViewItemsCountMatcher(ANY_COUNT)));
    }

    @Test
    public void elementsOnListHaveExpectedName() {
        givenThereAreSomeSuperHeroes(ANY_COUNT);

        startActivity();

        for (int i = 0; i < ANY_COUNT; i++) {
            onView(withId(R.id.recycler_view)).perform(RecyclerViewActions.scrollToPosition(i));
            onView(withText(PREMIUM_SUPER_HERO_BASE_NAME + i)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void avengersBadgeIsDisplayedOnAvengerHeroes() {
        givenThereAreSomeSuperHeroes(ANY_COUNT);

        startActivity();

        for (int i = 0; i < ANY_COUNT; i++) {
            if (i % 2 != 0) {
                onView(withId(R.id.recycler_view)).perform(RecyclerViewActions.scrollToPosition(i));
                onView(allOf(
                        hasSibling(withText(PREMIUM_SUPER_HERO_BASE_NAME + i)),
                        withId(R.id.iv_avengers_badge)))
                        .check(matches(isDisplayed()));
            }
        }
    }

    @Test
    public void avengersBadgeNotDisplayedOnNotAvengerHeroes() {
        givenThereAreSomeSuperHeroes(ANY_COUNT);

        startActivity();

        for (int i = 0; i < ANY_COUNT; i++) {
            if (i % 2 == 0) {
                onView(withId(R.id.recycler_view)).perform(RecyclerViewActions.scrollToPosition(i));
                onView(allOf(
                        hasSibling(withText(PREMIUM_SUPER_HERO_BASE_NAME + i)),
                        withId(R.id.iv_avengers_badge)))
                        .check(matches(not(isDisplayed())));
            }
        }
    }

    @Test
    public void detailDisplayedWhenClickingOnAvenger() {
        givenThereAreSomeSuperHeroes(ANY_COUNT);

        startActivity();
        onView(withId(R.id.recycler_view)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        intended(hasComponent(SuperHeroDetailActivity.class.getCanonicalName()));
    }

    private void givenThereAreNoSuperHeroes() {
        when(repository.getAll()).thenReturn(Collections.<SuperHero>emptyList());
    }

    private void givenThereAreSomeSuperHeroes(final int count) {
        when(repository.getByName(PREMIUM_SUPER_HERO_BASE_NAME + 0)).thenReturn(buildPremiumSuperHero(0));
        when(repository.getAll()).thenReturn(new ArrayList<SuperHero>() {{
            for (int i = 0; i < count; i++) {
                add(buildPremiumSuperHero(i));
            }
        }});
    }

    private SuperHero buildPremiumSuperHero(int i) {
        return new SuperHero(PREMIUM_SUPER_HERO_BASE_NAME + i,
                "https://i.ytimg.com/vi/mkqkxuTQUnU/hqdefault.jpg",
                i % 2 != 0,
                "This is the premium superhero, who beats any other one in the history of superheroes.");
    }

    private MainActivity startActivity() {
        return activityRule.launchActivity(null);
    }
}