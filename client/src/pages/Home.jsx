import React, { Component } from "react";

import Logo from "../images/logo.png";

export class Home extends Component {
  render() {
    return (
      <>
        <main className="window-fill page-container center column-flex center">
          <section className="page-title container center flex-row">
            <img src={Logo} alt="logo" className="main-lg" />
            <div className="title-container center column-flex">
              <div className="title main">
                <span className="title-letter">J</span>
                <span className="title-letter">a</span>
                <span className="title-letter">k</span>
                <span className="title-letter">l</span>
                <span className="title-letter">e</span>
              </div>
              <div className="slogan">That connects us all.</div>
            </div>
          </section>
          <div className="controls">
            <a href="/login" className="btn default-btn">
              Login
            </a>
            <a href="/signup" className="btn default-btn">
              Sign up
            </a>
          </div>
        </main>
      </>
    );
  }
}
