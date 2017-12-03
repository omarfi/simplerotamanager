# OFI Rota Manager (prototype)

Applikasjonen er utviklet etter forespørsel fra en potensiell arbeidsgiver for å demonstrere tekniske ferdigheter, samtidig som den adresserer et reelt behov - utvikleren har benyttet anledning til å levere en prototype av en mer 
omfattende løsning som er under arbeid.

OFI Rota Manager har som mål å assistere butikkdrivere med å organisere sine ansattes skiftplaner på en tidseffektiv og brukervennlig måte. Den kommer med en praktisk og intuitiv brukergrensesnitt som muliggjør raskt generering av excel baserte tjenesteplaner.

## Bruk
Applikasjonen er i produksjon på:

https://ofirotamanager.herokuapp.com/

Grensesnittet skal være intuitivt. For å populere dagene med skifter benyttes knappen "Legg inn skift" og deretter taster inn navn på ansatt, start tid og slutt tid. Skiftet kan gjøres repeterbar ved å kopiere til relevante dager. 

Etter at alle skiftene er lagt inn kan knappen "Generer tjenesteplan" benyttes for å produsere en excel fil som inneholder en skiftplan i en tabell med predefinert format, klar for utskrift. Ytterligere justeringer kan utføres direkte i excel.

### Begrensninger:
Det er foreløpig ikke mulig å legge inn en skift som strekker over flere datoer (typisk nattskift). Se [issue#1](https://github.com/omarfi/simplerotamanager/issues/1) 

## Teknologier og rammeverk
Applikasjonen bygger seg på 
- [JAVA 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
- [Spring Boot/REST](https://projects.spring.io/spring-boot/)
- [Maven](https://maven.apache.org/)
- [Apache POI](https://poi.apache.org/)
- [AngularJs](https://angularjs.org/)
- [Angular Bootstrap Calendar](https://github.com/mattlewis92/angular-bootstrap-calendar)
- [Bootstrap](http://getbootstrap.com/)
- [Bower](https://bower.io/)
