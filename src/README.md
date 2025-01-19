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
-  `SplitPayment` (parțial)

### Explicații implementări:
1. **Planuri și comisioane**:
    - Fiecare utilizator are acum un câmp `plan` care este inițializat cu `standard`/`student` în funcție de ocupația sa.
    - Planul poate fi modificat cu ajutorul clasei `UpgradePlan`, care verifică toate cerințele specificate în enunț și modifică planul în funcție de acestea.
    - Am adăugat clasa `Commission`, care returnează rata de comision în funcție de planul utilizatorului.

2. **Cashback**:
    - Lista de comercianți este reținută în `AppLogic` la începutul testului.
    - Clasa `Commerciant` gestionează informațiile despre comercianți și numărul de tranzacții efectuate la aceștia.
    - Fiecare cont include:
        - O listă de `Commerciant`.
        - Un câmp pentru suma totală cheltuită.
        - Trei câmpuri de tip boolean pentru verificarea cashback-ului de tipul "nrOfTransactions".
    - Cashback-ul este implementat pentru comanda `PayOnline`, unde:
        - La plata către un comerciant de tip `nrOfTransactions`, se incrementează contorul de tranzacții pentru comerciantul respectiv (contor prezent în `Commerciant`) și se verifică dacă se poate aplica cashback-ul.
        - Pentru `spendingThreshold`, suma cheltuită este adăugată la totalul cheltuielilor contului și se verifică dacă se poate aplica cashback-ul.

3. **SplitPayment**:
    - Comanda `SplitPayment` include acum două clase:
        - `EqualSplitPayment` (redenumită din implementarea anterioară).
        - `CustomSplitPayment` (pentru noua funcționalitate descrisă în enunț).
    - Clasa `PendingTransaction` reține informațiile despre o comandă de tip `splitPayment` în așteptare.
    - Fiecare utilizator are acum o listă de `PendingTransaction`.
    - Verificările pentru tranzacții de tip `splitPayment` includ:
        - Utilizarea clasei `VerifySplitPaymentBase` pentru a identifica conturile care refuză tranzacția sau comenzile invalide de accept/reject.
        - Adăugarea tranzacțiilor aprobate în lista de `PendingTransaction` a conturilor implicate.
    - În momentul în care se apelează comanda `printTransactions`:
        - Tranzacțiile în așteptare sunt executate până la timestamp-ul curent.
        - Tranzacțiile utilizatorului sunt sortate după timestamp și afișate.

> Din cauză că am ales o abordare mult prea complexă pentru această comandă, implementarea nu este completă. Am realizat acest lucru mult prea târziu și nu m-am putut încadra în limita de timp pentru modificarea codului.

## Design pattern-uri utilizate
- **Factory** pentru `AccountFactory` (cont clasic sau de economii).
- **Singleton** pentru `AppLogic`.
- **Command** pentru `CommandSelector` (fiecare comandă implementează interfața `Executable`).
- **Strategy** pentru `SplitPaymentStrategy` (două strategii: `EqualSplitPayment` și `CustomSplitPayment`).

## Feedback
- Prezentarea acestei etape a fost mai clară cu enunț mai bine explicat și exemple mai detaliate (deși ref-ul a fost ajutorul de nădejde).
- Pe partea cealaltă au fost mari probleme cu corectitudinea testelor (chiar si după ce au fost updatate), fiind și unul din motivele pentru care m-am apucat mai târziu de temă (no blame on you pentru că nu am reușit să implementez ce aveam de implementat).
