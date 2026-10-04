# Bibliotekshanteraren


> *Reflektionen är skriven på engelska, då jag är mer van och bekväm med det. Om det är ett krav att den skrivs på svenska översätter jag den givetvis.*


A command-line library management system written in Java for Laboration 1. It is built with plain fixed-size arrays, without the Collections Framework or Generics.

## Features

- Add books and members
- Loan and return books
- Search for books by title or author
- Browse all books with their status, sorted alphabetically
- View members ranked by their number of active loans

## Reflektion

This project is a command-line interface (CLI) application that simulates a small library management system. It allows the user to add books, add members, loan and return books, search for books by title or author, browse all books with their status sorted in alphabetical order and view a list of members ranked by their number of active loans. The code is split across four Java files: Book.java and BookLoan.java define two records, Member.java defines a class representing a library member, and Library.java is the main class holding the data and menu logic.

---
### Record vs klass

The reason for choosing to make Book and BookLoan records firstly comes down to the fact that both Book and BookLoan are just meant to store data that won't change. When making a new book the program requires the title, author and ISBN. The same goes for BookLoan, it takes a Member, a Book and the date the loan was created. Both store their data from creation and never change it. That is the biggest differentiating factor between a record and a class, records are immutable meaning they can't change after creation whilst a class is mutable and is fully able to change after its creation.

To keep the records as a place that only stores data I decided to make the `printBook()` method in the Library class instead of making it in the Book record. That way I get a record that only stores data and doesn't mix in anything else. The same goes for BookLoan, but it also has an overloaded constructor that lets the loan date default to the current date instead of requiring it to be passed in manually. In addition, it has a method (`getReturnDate()`) that calculates the return date by adding 14 days to the loan date. Since this calculation doesn't store or change any field, and only computes a value from the existing `loanDate` each time it's called, the record remains fully immutable even with this method present.

The reason the Member class is not a record and instead just a normal class is because of its fields. It has a member id which stores an integer, a name which stores a string but also an array that holds the members current active loans. Since the array storing the member's loans will get BookLoans added and removed depending on if the member borrows or returns a book, it means that the array changes and isn't immutable which makes it unable to function as a record. And since an array has a set size at its creation, if the user fills it up with BookLoans and then decides to loan another book, the class creates a new array with a larger size, then it copies the previous array and then finally it replaces the old array. That is another way the data the member class stores changes which makes it mutable.

---
### Array-hantering utan Generics

In the project I used arrays to store Books, BookLoans and Members. Since there is no way to know in advance how many of each the user might want to add, I needed a way to grow each array beyond its initial fixed size. I solved this by writing methods that create a new, larger array and copy over the existing elements. However, since each array stores a different type (Book, BookLoan or Member), I had to write three nearly identical methods: `extendBooksAndAdd()`, `extendMembersAndAdd()` and `extendLoansAndAdd()`. The only difference between them is the type they operate on.

If Generics were allowed, a single generic method using a type parameter (e.g. `<T>`) could handle all three array types at once, eliminating the duplication entirely. Without Generics, I'm forced into writing near-identical code for each type, which makes the array-handling logic far less reusable and introduces real code duplication.

With Collections allowed, this problem would largely disappear: a single `ArrayList<T>.add()` call handles growth, copying, and insertion automatically for any type, removing the need to write and maintain any of these extend-methods by hand.

---
### Null-hantering i fast-storlek-arrayer

The assignment required using arrays with a defined size at creation, meaning they contain null values until filled. That leads to having to null check anything that interacts with the array since there is a possibility that instead of it having a value it could be null. If the code doesn't have any null checks and the program tries to use something from an array assuming it has a value, it will cause the program to throw an exception (NullPointerException) and if that exception isn't caught (e.g. try-catch block) it will cause it to crash.

During development, this requirement for constant null-checking led to several real bugs. For example, a method that iterated over the bookLoans array without checking for null crashed with a NullPointerException as soon as it was called while the array wasn't yet full.
This made it clear how easy it is to overlook a null-check in just one of the many places that interact with these arrays, especially as the codebase grows.

---
### Linjär sökning

The search menu option allows the user to search for a book by either entering the books title or the author. It first loops through all the books in the books array and checks if any book has a title or author that includes the search. If a match is found, the book is added to a temporary array before continuing with the next iteration. When it is done with iterating through the entire array it checks if any match was found and then prints out all books that had a title or author that matched the search keyword. Since the search method has to iterate through the entire array to search it is what's called linear search.

---
### Begränsningar och kända avvägningar

One limitation I found was in the creation of members. When a new member is added it requires a name, but nothing stops two members with the same name from being added. This causes the `findMember(String)` method to always pick the first member when there is more than one member with the same name. For example, if two members are both named "Hannes", the program will always pick the one that was added first, and the second member is only reachable by using their unique member ID.

This was a deliberate choice. The assignment doesn't specify how duplicate names should be handled, and every member can still be found through their unique ID, so the system remains fully usable. Solving it would have required either blocking new members whose name already exists, or letting a name search display a numbered list of matching members and letting the user pick one, the same pattern already used in `search()` when multiple books match a query.

It is worth noting that this is a limitation of the name-lookup logic itself, not a consequence of using fixed-size arrays. The ambiguity would exist with any data structure, including a Collections-based one, unless duplicates were blocked or a disambiguation step was added.

---
### Reflektion: med Collections Framework

If the Collections Framework had been allowed, the biggest simplification would be the manual array handling. Both `ArrayList` and `HashMap` grow automatically and internally, so none of the create-copy-replace logic in `extendBooksAndAdd()`, `extendMembersAndAdd()` and `extendLoansAndAdd()` would be needed. Together with the `isFull()` and `getEmpty()` helper methods, that is a good part of Library.java that would disappear. For Books and BookLoans, where no key-based lookup is needed, an `ArrayList` would be the natural direct replacement for the plain arrays. Removing a returned loan would also become a single `remove()` call instead of searching for the loan and setting its index to null, which would remove most of the null checks I described earlier.

For Members, a `HashMap<Integer, Member>` using the member's existing unique id as the key would give automatic growth as well as direct lookup by ID, instead of the linear search that `findMember(int)` does. The same idea would work for Books with the ISBN as key, which would make the duplicate-ISBN check in `addBook()` a simple `containsKey()` call.

A Map would not, however, solve the duplicate-name limitation discussed above. Looking up a member by name would still be ambiguous, since several members can share a name, so a disambiguation step or a block on duplicate names would still be needed.


