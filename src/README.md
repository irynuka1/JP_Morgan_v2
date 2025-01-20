#### Coitu Sebastian-Teodor 324CA

## Modificări aduse la etapa 1
- Clasele au fost rearanjate pentru o mai bună organizare, iar comenzile se află acum în pachetul `commands` din `e_banking`.
- `AppLogic` este implementat acum cu un Singleton, pentru a asigura că există o singură instanță a clasei.
- Am modificat puțin `CommandSelector` pentru a implementa design pattern-ul **Command**.
- Am modificat unele mesaje de output după necesitate.

## Modificări aduse la etapa 2
### Comenzi implementate
Pentru această parte am implementat comenzile:
- `CashWithdrawal`
- `WithdrawSavings`
- `UpgradePlan`
-  `SplitPayment` (aproape complet)
- `Accept/Reject SplitPayment`

### Explicații implementări:
1. **Planuri și comisioane**:
    - Fiecare utilizator are acum un câmp `plan` care este inițializat cu `standard`/`student` în funcție de ocupația sa.
    - Planul poate fi modificat cu ajutorul clasei `UpgradePlan`, care verifică toate cerințele specificate în enunț și modifică planul în funcție de acestea.
    - De asemenea, planul poate fi modificat și în cazul în care un utilizator ce deține deja planul silver face minimum 5 plăți de peste 300RON. În acest caz se apelează în `PayOnline` metoda `freeUpgradeIfPossible` din clasa `UpgradePlan`.
    - Am adăugat clasa `Commission`, care returnează rata de comision în funcție de planul utilizatorului.

2. **Cashback**:
    - Lista de comercianți este reținută în `AppLogic` la începutul testului.
    - Clasa `Commerciant` gestionează informațiile despre comercianți și numărul de tranzacții efectuate la aceștia.
    - Fiecare cont include în plus față de etapa trecută:
        - O listă de `Commerciant`.
        - Un câmp `totalSum` pentru suma totală cheltuită la comercianți.
        - Trei câmpuri (`canGetFoodCashBack`, `canGetClothesCashBack`, `canGetTechCashBack`) de tip boolean pentru verificarea aplicării cashback-ului de tipul `nrOfTransactions`.
    - Cashback-ul este implementat pentru comanda `PayOnline`, unde:
        - La plata către un comerciant de tip `nrOfTransactions`, se incrementează contorul de tranzacții pentru comerciantul respectiv (contor prezent în clasa `Commerciant`) și se verifică dacă se poate aplica cashback-ul.
        - Pentru `spendingThreshold`, suma cheltuită este adăugată la totalul cheltuielilor contului și se verifică suma cashback-ului.

3. **SplitPayment**:
    - Pachetul `splitPayment` din `e_banking/commands` conține clasele necesare pentru implementarea comenzii `SplitPayment`.
    - Comanda `SplitPayment` poate fi acum de două tipuri:
        - `EqualSplitPayment` (redenumită din implementarea anterioară).
        - `CustomSplitPayment` (pentru noua funcționalitate descrisă în enunț).
    - Clasa `PendingTransaction` reține informațiile despre o comandă de tip `splitPayment` care așteaptă răspunsul user-ilor implicați.
    - Fiecare utilizator are acum o listă de `PendingTransaction`.
    - Verificarea pentru o tranzacție de tip `splitPayment` se realizează astfel:
        - In clasa `VerifySplitPaymentBase` se verifică răspunsurile utilizatorilor, iar cele de reject sunt reținute în array-ul `userNotFounds` din clasa `AppLogic`.
        - Odată ce au fost verificate toate răspunsurile, dacă acestea sunt pozitive, se adaugă tranzacția de split în listele de `PendingTransactions` ale utilizatorilor implicați, altfel tranzacția nu se mai realizează.
        - Pentru evitarea verificării multiple al aceluiași răspuns, am adăugat în `AppLogic` un array `verifiedTransactions` care reține timestamp-ul tranzacțiilor verificate.
    - În momentul în care se apelează comanda `printTransactions`:
        - Tranzacțiile în așteptare sunt executate până la timestamp-ul curent.
        - Tranzacțiile utilizatorului sunt sortate după timestamp (pentru a include noile tranzacții adăugate în output la timestamp-ul potrivit) și afișate.

4. **Accept/Reject SplitPayment**:
   - Output-ul comenzilor `AcceptSplitPayment` și `RejectSplitPayment` nevalide pentru o comandă de tip `splitPayment` sunt reținute în array-ul `userNotFounds` din clasa `AppLogic` (așa cum am zis mai sus).
   - În momentul în care întâlnesc una din comenzile de mai sus, în clasa `AcceptRejectPayment` se verifică dacă `userNotFounds` conține output-ul comenzii. Dacă nu este prezentă, înseamnă că comanda a fost validă și verificată în `VerifySplitPaymentBase`.

### Observații:
> Din cauză că am ales o abordare mult prea complexă pentru SplitPayment, implementarea nu este completă (mai sunt niște cazuri neabordate pentru afișarea mesajelor de "User not found"). Am realizat acest lucru mult prea târziu și nu m-am putut încadra în limita de timp pentru modificarea codului.

## Design pattern-uri utilizate
- **Factory** pentru `AccountFactory` (cont clasic sau de economii).
- **Singleton** pentru `AppLogic`.
- **Command** pentru `CommandSelector` (fiecare comandă implementează interfața `Executable`).
- **Strategy** pentru `SplitPaymentStrategy` (două strategii: `EqualSplitPayment` și `CustomSplitPayment`).

## Feedback
- Prezentarea acestei etape a fost mai clară cu enunț mai bine explicat și exemple mai detaliate (deși ref-ul a fost ajutorul de nădejde).
- Pe partea cealaltă au fost mari probleme cu corectitudinea testelor (chiar si după ce au fost updatate), fiind și unul din motivele pentru care m-am apucat mai târziu de temă (no blame on you pentru că nu am reușit să implementez toate cerințele).
