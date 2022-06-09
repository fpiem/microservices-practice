<!-- PROJECT LOGO -->
<br />
<p align="center">
  <a href="https://git-softeng.polito.it/master/group-04/ecommerce">
    <img src="https://git-softeng.polito.it/master/group-04/ecommerce/-/raw/master/docs/images/ecommerce_logo.png" alt="Logo" width="600" height="80">
  </a>

<h3 align="center">eCommerce</h3>

  <p align="center">
    A headless e-commerce web application
    <br />
    <a href="https://git-softeng.polito.it/master/group-04/ecommerce/-/blob/master/docs/Report.pdf"><strong>Read the report »</strong></a>
  </p>
</p>

<!-- TABLE OF CONTENTS -->
<details open="open">
  <summary>Table of Contents</summary>
  <ol>
    <li>
      <a href="#about-the-project">About The Project</a>
      <ul>
        <li><a href="#built-with">Built With</a></li>
      </ul>
    </li>
    <li>
      <a href="#getting-started">Getting Started</a>
      <ul>
        <li><a href="#prerequisites">Prerequisites</a></li>
        <li><a href="#installation">Installation</a></li>
      </ul>
    </li>
    <li><a href="#usage">Usage</a></li>
    <li><a href="#license">License</a></li>
    <li><a href="#contact">Contact</a></li>
  </ol>
</details>

<!-- ABOUT THE PROJECT -->

## About The Project

This project defines an implementation proposal for a headless e-commerce web application, developed in the Kotlin
programming language using the Spring Boot framework. The application leverages an architecture based on four
microservices, and features robustness to both logical and physical failures.

### Built With

This application was built leveraging on the following languages and frameworks:

* [Kotlin](https://kotlinlang.org/)
* [Spring Boot](https://spring.io/projects/spring-boot)
* [MongoDB](https://www.mongodb.com/)
* [Python](https://www.python.org/)
* [Docker](https://www.docker.com/)

<!-- GETTING STARTED -->

## Getting Started

This section provides instructions about how to set up the project locally. To get a local copy up and running please
follow the simple steps described below.

### Prerequisites

In order to run the installation commands, you need to have docker installed on your local computer or server. Please
refer to the [docker documentation](https://docs.docker.com/get-docker/) for installation guidance.

### Installation

1. Clone the repository
   ```sh
   git clone https://git-softeng.polito.it/master/group-04/ecommerce.git
   ```
2. Build the `.jar` files for the microservices
   ```sh
   docker-compose -f .\docker-compose-build.yml up --remove-orphans
   ```
3. Deploy the microservices
   ```sh
   docker-compose -p ecommerce -f .\docker-compose.yml up -d --remove-orphans
   ```

<!-- USAGE EXAMPLES -->

## Usage

Since the application is headless and does not provide a client-side rendering of the  information,
[Postman](https://www.postman.com/) was used as the reference platform for testing the APIs functionality.

A [Postman collection](https://www.postman.com/collection/) was created in order to provide an easy way of invoking
the set of available APIs (see `collection-name.json` for more information).

To import the collection into Postman:

1. Click **Import** and select `collection-name.json` (Postman will automatically recognize the type of file).
    
2. Click **Import** to bring the collection into Postman.

3. After making sure that all services are up and running, you can execute the APIs defined in the imported collection.

<!-- LICENSE -->

## License

Released under the MIT License. See `LICENSE.md` for more information.

<!-- CONTACT -->

## Contact

* Mattia Michelini\
  [s291551@studenti.polito.it](mailto:s291551@studenti.polito.it)


* Manuel Peli\
  [s291485@studenti.polito.it](mailto:s291485@studenti.polito.it)


* Francesco Piemontese\
  [s291491@studenti.polito.it](mailto:s291491@studenti.polito.it)


* Marco Rossini\
  [s291482@studenti.polito.it](mailto:s291482@studenti.polito.it)

Project Link: [https://git-softeng.polito.it/master/group-04/ecommerce](https://git-softeng.polito.it/master/group-04/ecommerce)
