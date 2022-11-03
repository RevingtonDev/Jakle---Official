import React, { Component } from "react";

export class Pagination extends Component {
  constructor(props) {
    super(props);

    const { pages, current } = this.props;
    this.data = [];

    if (pages <= 5) {
      for (let i = 1; i <= pages; i++) {
        this.data.push(i);
      }
    } else {
      if (current <= 3) {
        for (let i = 1; i <= 3; i++) {
          this.data.push(i);
        }
        // eslint-disable-next-line
        if (current == 3) this.data.push(4);
        this.data.push("...");
        this.data.push(pages);
      } else if (current > pages - 3) {
        this.data.push(1);
        this.data.push("...");
        // eslint-disable-next-line
        if (current == pages - 2) this.data.push(pages - 3);
        for (let i = pages - 2; i <= pages; i++) {
          this.data.push(i);
        }
      } else {
        this.data.push(1);
        this.data.push("...");
        for (let i = current - 1; i <= current + 1; i++) {
          this.data.push(i);
        }
        this.data.push("...");
        this.data.push(pages);
      }
    }
  }

  render() {
    const { callback, current } = this.props;
    return (
      <>
        <section className="row center">
          {this.data.map((elm) => {
            if (elm === "...")
              return <button className="btn pag-btn pag-btn-dummy">...</button>;
            else
              return (
                <button
                  className="btn pag-btn"
                    // eslint-disable-next-line
                  disabled={elm == current}
                  onClick={(e) => {
                    callback(elm);
                  }}
                >
                  {elm}
                </button>
              );
          })}
        </section>
      </>
    );
  }
}
