# Nowoczesna Java

## Podsumowanie najważniejszych zmian w każdym wydaniu LTS

Poniżej znajduje się szczegółowa lista kluczowych zmian językowych wprowadzonych w każdej wersji Long-Term Support (LTS)
Javy od Java 8 do Java 21

### Java 8 (Wydana w marcu 2014)

1. Wyrażenia Lambda
- Wprowadzono możliwości programowania funkcyjnego, pozwalające na przekazywanie zachowań jako parametrów.
- Umożliwiono pisanie bardziej zwięzłego i czytelnego kodu poprzez redukcję szablonowego kodu związanego z klasami anonimowymi.

2. Stream API
- Dostarczono nową abstrakcję do przetwarzania sekwencji elementów, wspierającą operacje takie jak map, filter i reduce.
- Ułatwiono równoległe przetwarzanie kolekcji, poprawiając wydajność i skalowalność.

3. Interfejsy funkcyjne
- Zdefiniowano interfejsy z jedną metodą abstrakcyjną, służące jako cele dla wyrażeń lambda i referencji do metod.
- Zwiększono możliwość pisania bardziej elastycznych i wielokrotnego użytku komponentów kodu.

4. Referencje do metod
- Udostępniono skróconą notację dla wyrażeń lambda wywołujących istniejące metody.
- Poprawiono czytelność i łatwość utrzymania kodu poprzez bezpośrednie odwoływanie się do metod.

5. Metody domyślne w interfejsach
- Umożliwiono interfejsom zawieranie domyślnych implementacji metod.
- Zwiększono kompatybilność wsteczną, pozwalając interfejsom ewoluować bez łamania istniejących implementacji.

6. Klasa Optional
- Rozwiązano problem referencji null poprzez dostarczenie obiektu kontenera, który może, ale nie musi, zawierać wartość różną od null.
- Zachęcono do lepszej obsługi potencjalnych wartości null, zmniejszając prawdopodobieństwo wystąpienia `NullPointerException`.

7. Nowe API daty i czasu (java.time)
- Wprowadzono kompleksowe i spójne API do manipulacji datą i czasem.
- Zastąpiono starsze klasy `java.util.Date` i `java.util.Calendar` bardziej niezawodnymi alternatywami.

8. Powtarzalne adnotacje i adnotacje typów
- Umożliwiono stosowanie wielu adnotacji tego samego typu na pojedynczym elemencie.
- Poprawiono sprawdzanie typów i przejrzystość adnotacji w kodzie.

### Java 11 (Wydana we wrześniu 2018)

1. Składnia zmiennych lokalnych dla parametrów Lambda
- Wprowadzono słowo kluczowe `var` dla parametrów lambda, zwiększając czytelność i umożliwiając adnotacje na parametrach.

2. Rozszerzone API String
- Dodano nowe metody takie jak `isBlank()`, `lines()`, `strip()`, `stripLeading()`, `stripTrailing()` i `repeat(int)`, ułatwiające bardziej wszechstronne manipulacje łańcuchami znaków.

3. Wsparcie dla Unicode 10
- Zaktualizowano platformę Java w celu obsługi najnowszych standardów Unicode, poprawiając internacjonalizację i kompatybilność z różnorodnymi zestawami znaków.

4. Usunięcie i deprecjacja funkcji
- Usunięto silnik JavaScript Nashorn, upraszczając platformę.
- Zdeprecjonowano kilka starszych API w celu redukcji redundancji i zachęcenia do korzystania z bardziej nowoczesnych alternatyw.

5. Ulepszenia klienta HTTP
- Ulepszono istniejący klient HTTP o obsługę HTTP/2 i WebSocket, zapewniając lepszą wydajność i wsparcie nowoczesnych protokołów.

6. Flight Recorder i inne ulepszenia JVM
- Dołączono ulepszenia na poziomie JVM, takie jak Flight Recorder do profilowania i monitorowania aplikacji przy minimalnym narzucie wydajnościowym.

Uwaga: Java 11 skupiła się głównie na ulepszeniach API, poprawie wydajności i usunięciu przestarzałych funkcji, zamiast wprowadzania istotnych nowych konstrukcji językowych.

### Java 17 (Wydana we wrześniu 2021)

1. Klasy i interfejsy zapieczętowane (Sealed Classes)
- Umożliwiono klasom i interfejsom ograniczanie, które inne klasy lub interfejsy mogą je rozszerzać lub implementować.
- Ułatwiono tworzenie bardziej kontrolowanych i przewidywalnych hierarchii typów.

2. Dopasowywanie wzorców dla `instanceof`
- Uproszczono sprawdzanie typów i rzutowanie, umożliwiając wyodrębnianie zmiennych w ramach operatora `instanceof`.
- Zmniejszono ilość szablonowego kodu związanego z rzutowaniem typów.

3. Bloki tekstowe (Text Blocks)
- Wprowadzono wieloliniowe literały łańcuchowe, zwiększając czytelność i łatwość utrzymania kodu obsługującego duże bloki tekstu, takie jak zapytania JSON lub SQL.

4. Rekordy (Records) (Sfinalizowane w Java 16)
- Dostarczono zwięzłą składnię do deklarowania klas będących przezroczystymi nośnikami niezmiennych danych.
- Zmniejszono ilość szablonowego kodu związanego z prostymi klasami przechowującymi dane.

5. Rozszerzone instrukcje `switch` (Funkcje w wersji Preview)
- Wprowadzono bardziej ekspresyjne i elastyczne konstrukcje `switch`, pozwalające na bardziej zwięzłe i czytelne struktury sterowania przepływem.

6. Ulepszone generatory liczb pseudolosowych (PRNG)
- Rozszerzono zestaw algorytmów PRNG dostępnych na platformie Java, zapewniając programistom więcej opcji generowania liczb losowych.

7. Foreign Function & Memory API (Inkubator)
- Wprowadzono API umożliwiające programom Java współpracę z kodem i danymi spoza środowiska uruchomieniowego Java, ułatwiając integrację z bibliotekami natywnymi.

### Java 21 (Wydana we wrześniu 2023)

1. Wzorce rekordów (Record Patterns) (Preview)
- Umożliwiono dekonstrukcję wartości rekordów, pozwalając na bardziej zwięzły i czytelny kod podczas pracy z rekordami.
- Ułatwiono dopasowywanie wzorców z rekordami w instrukcjach warunkowych i wyrażeniach.

2. Dopasowywanie wzorców dla `switch` (Drugi Preview)
- Rozszerzono instrukcje `switch` o obsługę dopasowywania wzorców, czyniąc je bardziej potężnymi i ekspresyjnymi.
- Umożliwiono konstrukcjom `switch` bardziej naturalne obsługiwanie złożonych zapytań zorientowanych na dane.

3. Wątki wirtualne (Virtual Threads) (Project Loom)
- Wprowadzono lekkie wątki zarządzane przez JVM, upraszczając programowanie współbieżne.
- Umożliwiono programistom pisanie wysoce współbieżnych aplikacji z poprawioną skalowalnością i wydajnością.

4. Kolekcje sekwencyjne (Sequenced Collections)
- Dostarczono uporządkowane wersje interfejsów kolekcji, zapewniające że elementy zachowują zdefiniowaną kolejność napotkania.
- Zwiększono przewidywalność i spójność podczas przetwarzania kolekcji.

5. Ulepszone wyrażenia Switch i zapieczętowane interfejsy
- Kontynuowano ulepszenia wyrażeń `switch`, czyniąc je bardziej niezawodnymi i wszechstronnymi.
- Dalsze udoskonalenia zapieczętowanych interfejsów dla bardziej kontrolowanych hierarchii typów.

6. Ulepszona obsługa łańcuchów znaków i optymalizacje wydajności
- Ulepszono podstawową implementację łańcuchów znaków w celu lepszej wydajności i zmniejszenia zużycia pamięci.
- Dołączono optymalizacje korzystne zarówno dla wydajności w czasie wykonania, jak i produktywności programistów.

7. Deprecjacja i usunięcie przestarzałych funkcji
- Kontynuowano proces deprecjacji i usuwania przestarzałych API i funkcji w celu uproszczenia platformy Java.
- Zachęcano do adopcji bardziej nowoczesnych i wydajnych alternatyw.

8. Ulepszone dopasowywanie wzorców i inferencja typów
- Rozszerzono możliwości dopasowywania wzorców poza podstawowe typy, umożliwiając bardziej złożone i zagnieżdżone wzorce.
- Ulepszono mechanizmy inferencji typów w celu zmniejszenia potrzeby jawnych deklaracji typów.

## Nowy cykl publikacji i jego wpływ na codzienne programowanie

Nowy cykl publikacji Javy, ustanowiony w ostatnich latach, znacząco przekształcił krajobraz codziennego
tworzenia oprogramowania. Odchodząc od tradycyjnych wieloletnich odstępów między wydaniami, Java stosuje teraz przewidywalny
sześciomiesięczny cykl wydań, zapewniając, że nowe funkcje, ulepszenia i poprawki wydajności są
dostarczane spójnie i regularnie. Co trzy lata wydawana jest wersja Long-Term Support (LTS),
zapewniająca stabilną i wspieraną podstawę dla przedsiębiorstw i projektów długoterminowych. Ten usprawniony cykl pozwala
programistom szybciej korzystać z najnowszych innowacji językowych i ulepszeń API, promując kulturę
ciągłego doskonalenia i innowacji. W konsekwencji zespoły programistyczne mogą wykorzystywać najnowocześniejsze narzędzia i funkcje
w celu zwiększenia produktywności, jakości kodu i wydajności aplikacji bez oczekiwania przez dłuższe okresy między głównymi
wydaniami. Jednak zwiększona częstotliwość aktualizacji wymaga również od organizacji przyjęcia bardziej zwinnych praktyk
utrzymaniowych, zapewniających kompatybilność i aktualność ich baz kodowych z najnowszymi wersjami Javy. Ogólnie
nowy cykl publikacji zachowuje równowagę między dostarczaniem szybkich postępów a utrzymaniem stabilności poprzez
wydania LTS, zwiększając tym samym efektywność, elastyczność i responsywność codziennego programowania w Javie.

## JDK i kwestie licencyjne

Java Development Kit (JDK) to niezbędny zestaw narzędzi dla programistów Java, dostarczający narzędzia potrzebne do
tworzenia, kompilowania, debugowania i uruchamiania aplikacji Java. Zawiera Java Runtime Environment (JRE),
interpreter/loader (Java), kompilator (javac), archiwizator (jar), generator dokumentacji (javadoc)
oraz inne narzędzia niezbędne do programowania w Javie. JDK stanowi fundamentalną platformę do budowania
aplikacji Java, zapewniając programistom dostęp do najnowszych funkcji językowych, bibliotek i środowisk uruchomieniowych.

Historycznie Oracle dostarczał oficjalne JDK na podstawie Oracle Binary Code License Agreement, które zezwalało na darmowe
użytkowanie do celów osobistych i programistycznych, ale wymagało licencji komercyjnej do użytku produkcyjnego w organizacjach.
Ten model licencjonowania stanowił wyzwanie dla firm dążących do wdrażania aplikacji Java na dużą skalę, ponieważ wprowadzał
potencjalne koszty i kwestie prawne.

W przeciwieństwie do tego, OpenJDK wyłonił się jako referencyjna implementacja open-source platformy Java Platform, Standard Edition (Java SE).
Licencjonowany na podstawie GNU General Public License, wersja 2, z wyjątkiem Classpath Exception (GPLv2+CE), OpenJDK oferował darmową
i otwartą alternatywę dla programistów i organizacji. Pozwoliło to na szerszą adopcję bez ograniczeń
licencjonowania własnościowego, promując bardziej inkluzywny i współpracujący ekosystem Javy.

Począwszy od Java 11, Oracle zmienił model licencjonowania swoich dystrybucji JDK. Oracle JDK zaczął wymagać
licencji komercyjnej do użytku produkcyjnego, zbliżając go do warunków licencyjnych OpenJDK. Ta zmiana skłoniła
wiele organizacji do ponownego rozważenia swoich strategii wdrażania Javy, prowadząc do zwiększonej adopcji OpenJDK i innych
dystrybucji open-source, takich jak Amazon Corretto, AdoptOpenJDK (teraz część Eclipse Adoptium), Azul Zulu i Red Hat OpenJDK.

Społeczność Javy w dużej mierze przyjęła przejście na dystrybucje JDK open-source, dostrzegając korzyści
zmniejszonych kosztów, zwiększonej przejrzystości i wspólnej innowacji. Oracle nadal wnosi wkład do OpenJDK, zapewniając
że pozostaje kamieniem węgielnym programowania w Javie. Tymczasem alternatywni dostawcy wyróżnili się oferując
specjalizowane funkcje, rozszerzone wsparcie i optymalizacje wydajności dostosowane do różnych przypadków użycia.

Patrząc w przyszłość, organizacje muszą zachować czujność w kwestii warunków licencyjnych i ewoluującego krajobrazu dystrybucji JDK.
W miarę jak Java kontynuuje rozwój z nowymi funkcjami i ulepszeniami, utrzymanie zgodności z umowami licencyjnymi i
wykorzystywanie odpowiedniej dystrybucji JDK będzie kluczowe dla utrzymania wydajnych i bezpiecznych praktyk programowania w Javie.

### Wybór implementacji i wersji JDK

Wybór odpowiedniej implementacji i wersji Java Development Kit (JDK) jest kluczowy dla zapewnienia wydajności, bezpieczeństwa i łatwości utrzymania aplikacji Java. Przy wielu dystrybucjach JDK i częstych wydaniach, podjęcie świadomej decyzji wymaga zrozumienia dostępnych opcji i oceny ich pod kątem specyficznych potrzeb projektu. Poniżej znajduje się kompleksowy przewodnik, który pomoże w nawigacji tego procesu wyboru.

#### Zrozumienie implementacji JDK

Dostępnych jest kilka implementacji JDK, każda z własnym zestawem funkcji, modeli licencjonowania i opcji wsparcia:

- Oracle JDK
    - Opis: Oryginalne JDK dostarczane przez Oracle, historycznie standard programowania w Javie.
    - Licencja: Od Java 11 Oracle JDK wymaga licencji komercyjnej do użytku produkcyjnego, choć jest darmowe do celów osobistych i programistycznych.
    - Wsparcie: Oracle oferuje komercyjne wsparcie i aktualizacje dla Oracle JDK.

- OpenJDK
    - Opis: Referencyjna implementacja open-source platformy Java Platform, Standard Edition (Java SE).
    - Licencja: Dystrybuowane na podstawie GNU General Public License, wersja 2, z wyjątkiem Classpath Exception (GPLv2+CE).
    - Wsparcie: Wspierane przez społeczność open-source i różnych dostawców oferujących komercyjne wsparcie.

- Amazon Corretto
    - Opis: Darmowa, wieloplatformowa, gotowa do produkcji dystrybucja OpenJDK od Amazon.
    - Licencja: Open-source na podstawie GPLv2+CE.
    - Wsparcie: Długoterminowe wsparcie z regularnymi aktualizacjami dostarczanymi przez Amazon.

- Eclipse Temurin (dawniej AdoptOpenJDK)
    - Opis: Szeroko adoptowana dystrybucja OpenJDK zarządzana przez Eclipse Foundation.
    - Licencja: Open-source na podstawie GPLv2+CE.
    - Wsparcie: Prowadzone przez społeczność z opcjami komercyjnego wsparcia dostępnymi przez partnerów.

- Azul Zulu
    - Opis: Certyfikowana, przetestowana i wspierana kompilacja OpenJDK od Azul Systems.
    - Licencja: Oferuje zarówno open-source (GPLv2+CE), jak i licencje komercyjne.
    - Wsparcie: Kompleksowe usługi wsparcia, w tym wsparcie długoterminowe (LTS).

- Red Hat OpenJDK
    - Opis: Kompilacje OpenJDK dostarczane przez Red Hat, zoptymalizowane do użytku korporacyjnego.
    - Licencja: Open-source na podstawie GPLv2+CE.
    - Wsparcie: Wspierane przez ofertę wsparcia korporacyjnego Red Hat.

- BellSoft Liberica JDK
    - Opis: Dystrybucja OpenJDK z dodatkowymi funkcjami, takimi jak wbudowane JDK i JavaFX.
    - Licencja: Open-source na podstawie GPLv2+CE, z dostępnymi opcjami komercyjnymi.
    - Wsparcie: Oferuje długoterminowe wsparcie i usługi komercyjnego wsparcia.

Wybór odpowiedniej wersji JDK jest równie ważny, ponieważ wpływa na dostępne funkcje, wydajność i długoterminowe wsparcie.
Oto jak podejść do wyboru wersji:

- Wydania Long-Term Support (LTS) vs Non-LTS
    - Wersje LTS: Te wersje, wydawane co trzy lata (np. Java 8, 11, 17, 21), otrzymują rozszerzone wsparcie i są idealne dla środowisk produkcyjnych wymagających stabilności.
    - Wersje Non-LTS: Wydawane co sześć miesięcy, te wersje zapewniają dostęp do najnowszych funkcji, ale mają krótszy cykl wsparcia, odpowiedni do eksperymentowania i śledzenia innowacji.

- Stabilność vs najnowsze funkcje
    - Potrzeby stabilności: Dla aplikacji krytycznych dla działania firmy, wybór wersji LTS zapewnia stabilną i wspieraną podstawę.
    - Wymagania funkcjonalne: Jeśli Twój projekt korzysta z najnowszych ulepszeń językowych lub poprawek wydajności, rozważ adopcję nowszej wersji Non-LTS, pamiętając o potrzebie częstszych aktualizacji.

- Wymagania projektu i zależności
    - Kompatybilność bibliotek i frameworków: Upewnij się, że zależności Twojego projektu są kompatybilne z wybraną wersją JDK, aby uniknąć problemów z integracją.
    - Kwestie kodu legacy: Dla projektów ze znaczną ilością kodu legacy, utrzymanie spójności ze starszą wersją JDK może zmniejszyć nakład pracy na refaktoryzację.

- Harmonogramy wsparcia i cykl życia
    - Koniec publicznych aktualizacji: Bądź świadomy harmonogramów wsparcia dla każdej wersji JDK, aby planować migracje przed datami końca życia (EOL).
    - Wsparcie specyficzne dla dostawcy: Różne dystrybucje JDK mogą oferować różne czasy trwania wsparcia, więc dostosuj swój wybór do możliwości utrzymaniowych organizacji.

- Bezpieczeństwo i zgodność
    - Poprawki bezpieczeństwa: Wybierz wersję JDK, która otrzymuje regularne aktualizacje bezpieczeństwa w celu ochrony przed podatnościami.
    - Wymagania zgodności: Upewnij się, że wybrane JDK jest zgodne z regulacyjnymi i bezpieczeństwa standardami Twojej organizacji.

## Strategie migracji

Przy przechodzeniu na inną implementację JDK lub aktualizacji do nowszej wersji, wykonaj następujące kroki, aby zapewnić płynną migrację:

- Testy kompatybilności: Sprawdź, czy Twoja aplikacja działa poprawnie na nowym JDK, przeprowadzając kompleksowe testy, w tym testy jednostkowe, integracyjne i wydajnościowe.
- Weryfikacja zależności: Upewnij się, że wszystkie biblioteki i frameworki firm trzecich używane w projekcie są kompatybilne z docelową wersją i implementacją JDK.
- Benchmarking wydajności: Porównaj metryki wydajności między obecnym a nowym JDK, aby zidentyfikować ewentualne poprawy lub regresje.
- Stopniowe wdrażanie: Wdróż nowe JDK w środowiskach testowych przed wdrożeniem na produkcję, aby monitorować zachowanie i proaktywnie rozwiązywać problemy.
- Plany kopii zapasowych i wycofania: Utrzymuj kopie zapasowe i ustal procedury wycofania, aby powrócić do poprzedniej wersji JDK w przypadku krytycznych problemów podczas migracji.
- Dokumentacja i szkolenia: Zaktualizuj dokumentację projektu, aby odzwierciedlała szczegóły nowego JDK, i zapewnij szkolenie zespołowi programistycznemu na temat nowych funkcji lub zmian.

## Zarządzanie wieloma wersjami Javy

Programiści często muszą pracować z wieloma wersjami Javy, aby testować kompatybilność, korzystać z nowych funkcji językowych lub
utrzymywać aplikacje legacy. Dwa popularne narzędzia do zarządzania i przełączania między różnymi wersjami Javy to SDKMAN
i JVMS. Ten artykuł opisuje, jak używać tych narzędzi, podając przykłady wiersza poleceń i najlepsze praktyki zarządzania
wieloma wersjami Javy na jednym systemie.

### Korzystanie z SDKMAN

SDKMAN to popularne narzędzie wiersza poleceń do zarządzania równoległymi wersjami różnych SDK, w tym wielu dystrybucji Javy.
Zapewnia łatwy sposób instalacji, przełączania i konfiguracji środowisk Javy.

Uruchom następujące polecenie w terminalu, aby zainstalować SDKMAN:

```bash
curl -s "https://get.sdkman.io" | bash
```

Postępuj zgodnie z wyświetlanymi instrukcjami (które zazwyczaj obejmują ponowne uruchomienie terminala lub załadowanie skryptu inicjalizacyjnego SDKMAN).

Po zainstalowaniu możesz wyświetlić wszystkie dostępne dystrybucje i wersje Javy, uruchamiając:

```bash
sdk list java
```

To polecenie wyświetla tabelę z różnymi dostawcami i identyfikatorami wersji (na przykład OpenJDK, Zulu, Temurin).

Aby zainstalować konkretną wersję Javy, użyj polecenia install z identyfikatorem wersji z listy:

```bash
sdk install java 17.0.2-tem
```

Po instalacji przełącz się na tę wersję za pomocą:

```bash
sdk use java 17.0.2-tem
```

Możesz również ustawić wersję Javy jako domyślną:

```bash
sdk default java 17.0.2-tem
```

Sprawdź aktywną wersję Javy, uruchamiając:

```bash
java -version
```

To potwierdza, że środowisko odzwierciedla Twój wybór SDKMAN.

### Korzystanie z JVMS

JVMS (Java Version Manager) to kolejne narzędzie zaprojektowane specjalnie do przełączania między wieloma wersjami Javy.
Podczas gdy SDKMAN zarządza różnymi SDK, JVMS skupia się na Javie i zapewnia lekkie podejście do przełączania między
JDK bez trwałej zmiany ścieżek systemowych.

JVMS można zazwyczaj zainstalować, klonując jego repozytorium i dodając go do skryptu startowego powłoki.
Na przykład, w systemie uniksowym:

```bash
git clone https://github.com/patrickfav/jvms.git ~/jvms
echo 'export PATH="$HOME/jvms/bin:$PATH"' >> ~/.bashrc
source ~/.bashrc
```

*Uwaga:* Dostosuj kroki instalacji zgodnie z instrukcjami podanymi w [repozytorium JVMS](https://github.com/patrickfav/jvms) lub jego dokumentacji.

Wyświetl dostępne zainstalowane wersje zarządzane przez JVMS poleceniem podobnym do:

```bash
jvms list
```

Aby dodać nową wersję Javy, postępuj zgodnie z instrukcjami JVMS (może to obejmować podanie ścieżki do instalacji Javy
lub użycie zintegrowanych funkcji pobierania, jeśli są dostępne).

Przełączaj między zainstalowanymi wersjami za pomocą polecenia takiego jak:

```bash
jvms use 17.0.2
```

To polecenie tymczasowo ustawia określoną wersję jako aktywną w bieżącej sesji terminala. Aby sprawdzić aktywną wersję:

```bash
java -version
```

Zarówno SDKMAN, jak i JVMS pozwalają na przełączanie wersji Javy w locie, ułatwiając integrację ze skryptami budowania,
potokami ciągłej integracji lub środowiskami programistycznymi, w których wymagane są konkretne wersje. Możesz tworzyć skrypty
zmian wersji jako część konfiguracji projektu, aby zapewnić spójność między maszynami programistycznymi.
