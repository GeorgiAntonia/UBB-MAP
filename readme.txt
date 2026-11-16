Configurarea repository-urilor se face in src/main/resources/config.properties.
repository.type poate fi MEMORY, FILE sau DATABASE. RepositoryFactory creeaza ambele
repository-uri pentru tipul configurat.

Pentru baza de date, se seteaza repository.type=DATABASE. Repository-urile DB sunt
implementate cu JDBC si folosesc tabelele `users` si `friendships` din
`data/schema.sql`. Proiectul foloseste PostgreSQL.

Repository-urile DB folosesc `PreparedStatement` pentru interogari si leaga valorile
prin parametri (`?`), astfel incat date precum `'); DROP TABLE users; --` sunt
salvate ca text obisnuit si nu sunt executate ca SQL. Utilizatorii sunt stersi dupa
ID, iar operatiile de creare si stergere a unei prietenii folosesc ID-urile celor
doi utilizatori.

Cheile straine din `friendships` folosesc `ON DELETE CASCADE` pentru a pastra
integritatea referentiala chiar daca utilizatorul este sters direct din baza de date.
Serviciul sterge explicit prieteniile inaintea utilizatorului si pentru a actualiza
listele de prieteni tinute in memorie.

Repository-urile FILE citesc datele la pornire si rescriu fisierul dupa fiecare
salvare, stergere sau actualizare. Daca fisierul lipseste, repository-ul porneste gol
si fisierul se creeaza la prima salvare. Liniile invalide sunt raportate cu numarul
liniei si ignorate.

Utilizatorii sunt identificati prin ID-ul `BIGINT` generat de repository-ul de
persistenta; in modul DATABASE, acesta este generat de coloana identity a bazei de
date. Numele si prenumele sunt pastrate separat. Prieteniile sunt identificate prin
perechea ordonata de ID-uri `(id1, id2)`, aliniata cheii primare si constrangerii
`id1 < id2` din baza de date. Fisierele folosesc formatul
`id;prenume;nume` pentru utilizatori si `id1;id2;data` pentru prietenii.
Formatele anterioare fara ID sunt acceptate la incarcarea fisierelor; urmatoarea
modificare rescrie fisierul in formatul nou.

Validarea datelor: validatorul este folosit de repository la salvarea entitatilor,
inclusiv la incarcarea lor din fisiere. Astfel, sunt validate atat datele introduse
prin aplicatie, cat si cele citite din fisiere. Verificarea existentei utilizatorilor
pentru o prietenie se face in PrietenieService, deoarece aceasta regula implica
repository-ul utilizatorilor.


- DIP: UtilizatorService si PrietenieService primesc Repository-uri, nu clase concrete.
- OCP: repository-urile pot fi inlocuite fara modificarea service-ului si UI-ului.
  RepositoryFactory trebuie modificat cand se adauga un tip nou de persistenta.
  Un Abstract Factory ar permite adaugarea unei fabrici noi fara schimbarea unei
  fabrici existente, dar necesita interfata si clase de fabrica separate.
- SRP: conversia linie-entitate apartine subclaselor; citirea/scrierea comuna este
  in AbstractFileRepository; UI-ul gestioneaza interactiunea.
- LSP: repository-urile concrete implementeaza contractul Repository.

- Factory: RepositoryFactory selecteaza implementarea repository-urilor.
- Template Method: AbstractFileRepository implementeaza citirea si scrierea, iar
  subclasele definesc extractEntity si createEntityAsString.
- Strategy: Validator este furnizat repository-ului.
- Decorator: BufferedReader si BufferedWriter sunt folosite pentru operatiile cu
  fisiere

Interfete functionale si cautari:
- Validator<T> este o interfata functionala. In Main, validarea utilizatorului este
  compusa cu andThen si o lambda adauga regula ca numele si prenumele sa fie diferite.
  Validatorul prieteniei este definit tot printr-o lambda.
- Repository.findAll(Predicate<E>) filtreaza rezultatele, iar CriteriiUtilizator
  furnizeaza predicate care pot fi combinate.
- RepositoryFactory foloseste map-uri de Function pentru repository-urile MEMORY
  si DATABASE; repository-ul FILE pentru prietenii primeste si repository-ul
  utilizatorilor pentru a migra fisierele cu chei bazate pe nume.
- Sunt folosite referinte la metode pentru metode ale unui obiect oarecare si ale
  unui obiect anume, de exemplu Prietenie::getId, this::createEntityAsString,
  friendships::delete si System.out::println.

Decizii pentru API:
- Repository.findOne continua sa intoarca null cand entitatea nu exista. Serviciul
  expune cautarea utilizatorului ca Optional prin Optional.ofNullable; Optional nu
  este folosit pentru campuri sau parametri.
- Interfata Repository continua sa intoarca Iterable la findAll(); metoda cu filtru
  intoarce List. Iterable este transformat in stream printr-o metoda de tip
  StreamSupport.stream(...).
- Pentru repository-urile pe fisier se pastreaza Template Method, cu
  AbstractFileRepository si metode de conversie definite de subclase. Citirea
  foloseste Files.lines intr-un try-with-resources, iar scrierea foloseste Files.write
  cu liniile colectate dintr-un stream.

Metoda                                      Inainte                             Dupa
PrietenieService.delPrieteniiUtilizator     parcurgere si conditie              map, filter, forEach
PrietenieService.getNrComunitati            contor si parcurgerea userilor      filter, peek, count
PrietenieService.visitCommunity             parcurgerea vecinilor               filter, forEach, recursivitate
PrietenieService.findLongestPathInComponent parcurgerea si maximul              mapToInt, max, orElse
AbstractFileRepository.writeToFile          construire linii prin parcurgere    map, collect, Files.write

Pipeline-urile folosesc operatii intermediare precum filter, map si mapToInt,
urmate de operatii terminale precum count, max, collect sau forEach. DFS-ul pentru
comunitati si cautarea celui mai lung drum raman recursive; vecinii sunt procesati
cu stream-uri. Singura bucla clasica pastrata este while din UI.start, necesara
repetarii meniului pana la alegerea iesirii; nu este folosita pentru parcurgerea
unei colectii.
